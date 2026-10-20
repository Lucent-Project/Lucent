
use jni::objects::{JByteArray, JClass, JFloatArray};
use jni::sys::{jboolean, jbyteArray, jfloat, jint, JNI_FALSE, JNI_TRUE};
use jni::JNIEnv;

use aes_gcm::aead::{Aead, KeyInit, Payload};
use aes_gcm::{Aes256Gcm, Key, Nonce};
use pbkdf2::pbkdf2_hmac;
use sha2::Sha256;

use std::panic::{catch_unwind, AssertUnwindSafe};


fn bytes(env: &JNIEnv, arr: &JByteArray) -> Option<Vec<u8>> {
    env.convert_byte_array(arr).ok()
}

fn to_jbyte_array(env: &JNIEnv, data: &[u8]) -> jbyteArray {
    match env.byte_array_from_slice(data) {
        Ok(a) => a.into_raw(),
        Err(_) => std::ptr::null_mut(),
    }
}


#[no_mangle]
pub extern "system" fn Java_com_lucent_app_nativebridge_LucentNative_nativePbkdf2Sha256(
    env: JNIEnv,
    _class: JClass,
    password: JByteArray,
    salt: JByteArray,
    iterations: jint,
    key_len: jint,
) -> jbyteArray {
    let result = catch_unwind(AssertUnwindSafe(|| {
        let pw = bytes(&env, &password)?;
        let st = bytes(&env, &salt)?;
        if iterations <= 0 || key_len <= 0 || key_len > 1024 {
            return None;
        }
        let mut out = vec![0u8; key_len as usize];
        pbkdf2_hmac::<Sha256>(&pw, &st, iterations as u32, &mut out);
        Some(out)
    }));
    match result {
        Ok(Some(out)) => to_jbyte_array(&env, &out),
        _ => std::ptr::null_mut(),
    }
}


fn gcm_cipher(key: &[u8]) -> Option<Aes256Gcm> {
    if key.len() != 32 {
        return None;
    }
    Some(Aes256Gcm::new(Key::<Aes256Gcm>::from_slice(key)))
}

#[no_mangle]
pub extern "system" fn Java_com_lucent_app_nativebridge_LucentNative_nativeAesGcmSeal(
    env: JNIEnv,
    _class: JClass,
    key: JByteArray,
    iv: JByteArray,
    aad: JByteArray,
    plaintext: JByteArray,
) -> jbyteArray {
    let result = catch_unwind(AssertUnwindSafe(|| {
        let key = bytes(&env, &key)?;
        let iv = bytes(&env, &iv)?;
        let aad = bytes(&env, &aad)?;
        let pt = bytes(&env, &plaintext)?;
        if iv.len() != 12 {
            return None;
        }
        let cipher = gcm_cipher(&key)?;
        cipher
            .encrypt(Nonce::from_slice(&iv), Payload { msg: &pt, aad: &aad })
            .ok()
    }));
    match result {
        Ok(Some(ct)) => to_jbyte_array(&env, &ct),
        _ => std::ptr::null_mut(),
    }
}

#[no_mangle]
pub extern "system" fn Java_com_lucent_app_nativebridge_LucentNative_nativeAesGcmOpen(
    env: JNIEnv,
    _class: JClass,
    key: JByteArray,
    iv: JByteArray,
    aad: JByteArray,
    sealed: JByteArray,
) -> jbyteArray {
    let result = catch_unwind(AssertUnwindSafe(|| {
        let key = bytes(&env, &key)?;
        let iv = bytes(&env, &iv)?;
        let aad = bytes(&env, &aad)?;
        let ct = bytes(&env, &sealed)?;
        if iv.len() != 12 || ct.len() < 16 {
            return None;
        }
        let cipher = gcm_cipher(&key)?;
        cipher
            .decrypt(Nonce::from_slice(&iv), Payload { msg: &ct, aad: &aad })
            .ok()
    }));
    match result {
        Ok(Some(pt)) => to_jbyte_array(&env, &pt),
        _ => std::ptr::null_mut(),
    }
}


#[cfg(test)]
mod tests {
    use super::*;

    fn hex(data: &[u8]) -> String {
        data.iter().map(|b| format!("{:02x}", b)).collect()
    }

    #[test]
    fn pbkdf2_matches_reference() {
        let salt: Vec<u8> = (0u8..16).collect();
        let mut out = [0u8; 32];
        pbkdf2_hmac::<Sha256>(b"Lucent-backup-passphrase-v1", &salt, 10_000, &mut out);
        assert_eq!(
            hex(&out),
            "77297f53ee820b664c9fe17f392f60aa8fedbd42b5d309d5542b0ca07469aaf7"
        );
    }

    #[test]
    fn aes_gcm_matches_reference_and_round_trips() {
        let key: Vec<u8> = (0u8..32).collect();
        let iv: Vec<u8> = (0u8..12).collect();
        let aad = [1u8, 0, 0, 0, 7];
        let cipher = gcm_cipher(&key).unwrap();
        let ct = cipher
            .encrypt(Nonce::from_slice(&iv), Payload { msg: b"hello lucent", aad: &aad })
            .unwrap();
        assert_eq!(hex(&ct), "2f67ba77aac5ae6eee24f9ff577f858f60177ef51ef87b847946a4fe");
        let pt = cipher
            .decrypt(Nonce::from_slice(&iv), Payload { msg: &ct, aad: &aad })
            .unwrap();
        assert_eq!(pt, b"hello lucent");
        let mut bad = ct.clone();
        bad[0] ^= 1;
        assert!(cipher
            .decrypt(Nonce::from_slice(&iv), Payload { msg: &bad, aad: &aad })
            .is_err());
    }
}


#[no_mangle]
pub extern "system" fn Java_com_lucent_app_nativebridge_LucentNative_nativeCpuHasAvx2(
    _env: JNIEnv,
    _class: JClass,
) -> jboolean {
    #[cfg(target_arch = "x86_64")]
    {
        if std::arch::is_x86_feature_detected!("avx2") { JNI_TRUE } else { JNI_FALSE }
    }
    #[cfg(not(target_arch = "x86_64"))]
    {
        JNI_TRUE
    }
}
