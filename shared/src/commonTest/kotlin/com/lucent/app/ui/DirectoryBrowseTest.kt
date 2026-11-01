package com.lucent.app.ui

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DirectoryBrowseTest {

    private fun tree(): File {
        val root = File(System.getProperty("java.io.tmpdir"), "lucent-browse-${System.nanoTime()}").canonicalFile
        File(root, "Beta").mkdirs()
        File(root, "alpha").mkdirs()
        File(root, ".hidden").mkdirs()
        File(root, "notes.txt").writeText("x")
        return root
    }

    private fun slash(path: String): String = path.replace(File.separatorChar, '/')

    @Test
    fun normalizeKeepsRootsAndDropsTrailingSeparators() {
        assertEquals("/sdcard/Documents", DirectoryBrowse.normalize("/sdcard/Documents/"))
        assertEquals("/", DirectoryBrowse.normalize("/"))
        assertEquals("/sdcard/Documents", DirectoryBrowse.normalize("  /sdcard/Documents  "))
        assertEquals("", DirectoryBrowse.normalize("   "))
        assertEquals("C:\\", DirectoryBrowse.normalize("C:\\"))
        assertEquals("C:\\", DirectoryBrowse.normalize("c:/"))
        assertEquals("C:\\", DirectoryBrowse.normalize("c:"))
        assertEquals("C:\\Users\\Alice", DirectoryBrowse.normalize("C:/Users/Alice/"))
        assertEquals("C:\\Users\\Alice", DirectoryBrowse.normalize("C:\\Users\\Alice\\"))
    }

    @Test
    fun parentWalksUpAndStopsAtTheRoot() {
        val root = tree()
        val nested = File(root, "alpha").path
        val up = DirectoryBrowse.parent(nested).orEmpty()
        assertEquals(DirectoryBrowse.normalize(root.path), DirectoryBrowse.normalize(up))
        assertNull(DirectoryBrowse.parent("/"))
        assertTrue(DirectoryBrowse.isRoot("/"))
        assertEquals("C:\\Users", DirectoryBrowse.parent("C:\\Users\\Alice"))
        assertEquals("C:\\", DirectoryBrowse.parent("C:\\Users"))
        assertNull(DirectoryBrowse.parent("C:\\"))
        assertTrue(DirectoryBrowse.isRoot("C:\\"))
    }

    @Test
    fun listKeepsOnlyFoldersAndSortsThemByName() {
        val root = tree()
        val listed = DirectoryBrowse.list(root.path, showHidden = false).orEmpty()
        assertEquals(listOf("alpha", "Beta"), listed.map { it.name })
        assertEquals(DirectoryBrowse.normalize(File(root, "alpha").path), listed.first().path)
    }

    @Test
    fun listRevealsHiddenFoldersWhenAsked() {
        val root = tree()
        val listed = DirectoryBrowse.list(root.path, showHidden = true).orEmpty()
        assertEquals(listOf(".hidden", "alpha", "Beta"), listed.map { it.name })
    }

    @Test
    fun listReportsUnreadableTargetsInsteadOfPretendingTheyAreEmpty() {
        val root = tree()
        assertNull(DirectoryBrowse.list(File(root, "notes.txt").path, showHidden = false))
        assertNull(DirectoryBrowse.list(File(root, "missing").path, showHidden = false))

        val emptyDir = File(root, "empty-dir").apply { mkdirs() }
        val emptyOutcome = DirectoryBrowse.listOutcome(emptyDir.path, showHidden = false)
        assertIs<DirectoryListOutcome.Listed>(emptyOutcome)
        assertTrue(emptyOutcome.entries.isEmpty())

        val fileOutcome = DirectoryBrowse.listOutcome(File(root, "notes.txt").path, showHidden = false)
        assertIs<DirectoryListOutcome.Unreadable>(fileOutcome)
        assertEquals(UnreadableKind.NOT_A_DIRECTORY, fileOutcome.kind)

        val missingOutcome = DirectoryBrowse.listOutcome(File(root, "missing").path, showHidden = false)
        assertIs<DirectoryListOutcome.Unreadable>(missingOutcome)
        assertEquals(UnreadableKind.NOT_FOUND, missingOutcome.kind)

        val restrictedOutcome = DirectoryBrowse.listOutcome(
            "/storage/emulated",
            showHidden = false,
            android = true,
            storageGranted = true
        )
        assertIs<DirectoryListOutcome.Unreadable>(restrictedOutcome)
        assertEquals(UnreadableKind.RESTRICTED_INTERMEDIATE, restrictedOutcome.kind)

        val permissionOutcome = DirectoryBrowse.listOutcome(
            "/storage/emulated/0",
            showHidden = false,
            android = true,
            storageGranted = false
        )
        assertIs<DirectoryListOutcome.Unreadable>(permissionOutcome)
        assertEquals(UnreadableKind.PERMISSION_REQUIRED, permissionOutcome.kind)
    }

    @Test
    fun createMakesTheFolderAndRefusesBlankNames() {
        val root = tree()
        val made = DirectoryBrowse.create(root.path, "made-here")
        assertEquals(DirectoryBrowse.normalize(File(root, "made-here").path), made)
        assertTrue(File(made!!).isDirectory)
        assertEquals(made, DirectoryBrowse.create(root.path, "made-here"))
        assertNull(DirectoryBrowse.create(root.path, "   "))
    }

    @Test
    fun ancestorsRunFromTheRootDownToTheFolder() {
        val root = tree()
        val nested = File(root, "alpha").path
        val chain = DirectoryBrowse.ancestors(nested)
        assertTrue(chain.size >= 2, chain.toString())
        assertEquals("alpha", chain.last().name)
        assertEquals(DirectoryBrowse.normalize(nested), DirectoryBrowse.normalize(chain.last().path))
        chain.zipWithNext().forEach { (parentCrumb, childCrumb) ->
            assertEquals(
                DirectoryBrowse.normalize(parentCrumb.path),
                DirectoryBrowse.normalize(DirectoryBrowse.parent(childCrumb.path).orEmpty())
            )
        }

        val winChain = DirectoryBrowse.ancestors("C:\\Users\\Alice")
        assertEquals(listOf("C:\\", "Users", "Alice"), winChain.map { it.name })
        assertEquals(listOf("C:\\", "C:\\Users", "C:\\Users\\Alice"), winChain.map { it.path })
    }

    @Test
    fun ancestorsAndSafeParentStopAtNamedPlatformRootsInsteadOfUnreadableIntermediates() {
        val root = tree()
        val nested = File(root, "alpha/sub").apply { mkdirs() }
        val configuredRoots = listOf(
            StorageRoot("Shared storage", root.path, StorageRootKind.SHARED)
        )
        val crumbs = DirectoryBrowse.ancestorsForRoots(nested.path, configuredRoots)
        assertEquals(listOf("Shared storage", "alpha", "sub"), crumbs.map { it.name })
        assertEquals(DirectoryBrowse.normalize(root.path), crumbs.first().path)

        val upFromSub = DirectoryBrowse.safeParent(nested.path, configuredRoots)
        assertIs<SafeParentTarget.Folder>(upFromSub)
        assertEquals(DirectoryBrowse.normalize(File(root, "alpha").path), upFromSub.path)

        val upFromRoot = DirectoryBrowse.safeParent(root.path, configuredRoots)
        assertEquals(SafeParentTarget.RootsOverview, upFromRoot)

        val upFromRestricted = DirectoryBrowse.safeParent(
            "/storage/emulated",
            listOf(StorageRoot("Shared storage", "/storage/emulated/0", StorageRootKind.SHARED)),
            android = true,
            storageGranted = false
        )
        assertEquals(SafeParentTarget.RootsOverview, upFromRestricted)
    }

    @Test
    fun aTypedPathIsResolvedAgainstTheFolderOnScreen() {
        val root = tree()
        val deep = File(root, "alpha").path
        assertEquals(slash(deep), slash(DirectoryBrowse.resolve("alpha", root.path)))
        assertEquals(
            slash(DirectoryBrowse.normalize(root.path)),
            slash(DirectoryBrowse.normalize(DirectoryBrowse.resolve("..", deep)))
        )
        assertEquals(
            slash(DirectoryBrowse.normalize(root.path)),
            slash(DirectoryBrowse.normalize(DirectoryBrowse.resolve("", root.path)))
        )
        assertEquals("C:\\Users\\Bob", DirectoryBrowse.resolve("Bob", "C:\\Users"))
        assertEquals("C:\\", DirectoryBrowse.resolve("..", "C:\\Users"))
    }

    @Test
    fun startingPointPrefersARealListableFolderAndSkipsRestrictedIntermediates() {
        val root = tree()
        assertEquals(DirectoryBrowse.normalize(root.path), DirectoryBrowse.startingPoint(root.path))
        assertTrue(File(DirectoryBrowse.startingPoint(File(root, "nowhere").path)).isDirectory)

        val fallbackRoot = tree()
        val start = DirectoryBrowse.startingPoint(
            wanted = "/storage/emulated",
            roots = listOf(
                StorageRoot("Shared storage", "/storage/emulated/0", StorageRootKind.SHARED, requiresPermission = true),
                StorageRoot("App files", fallbackRoot.path, StorageRootKind.APP_PRIVATE)
            ),
            android = true,
            storageGranted = false
        )
        assertEquals(DirectoryBrowse.normalize(fallbackRoot.path), start)
        assertFalse(DirectoryBrowse.isAndroidRestrictedIntermediate(start))
    }
}
