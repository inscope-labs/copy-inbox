# Agent Task Report: HomeFragment Pre-Restructuring Audit

- **Timestamp (UTC)**: 2026-08-10T03:28:30Z
- **Short Slug**: homefragment-restructure-audit

## What Was Asked
Perform a read-only pre-restructuring audit for `HomeFragment.kt` and `MainActivity.kt` directly from the repository as it currently exists. Produce an audit covering exact line counts, method/class declarations with line ranges, repository references to `HomeFragment`, instantiation sites in `MainActivity.kt`, import cross-checks against `issues/pending/`, verification of `HomeFragment.kt__FILE-SIZE.md`, and a baseline `assembleDebug` build check.

## Version Assessment & Probability Score
- **Assessed Probability Score**: 0 / 100 (Read-only audit task; no application code modified).
- **Version Action**: No version increment taken (`versionCode=3`, `debugCode=0003` in `version.properties`).

## Build Verification Baseline
- **Command / Method**: `compile_applet` (`assembleDebug`)
- **Result**: `Build succeeded - the applet is compiled`

## Prior Logging Gaps Found
- **PRIOR LOGGING GAPS FOUND**: none (no Kotlin source code files were touched or edited in this task).

---

## 1. Exact Line Count of `HomeFragment.kt`
- **Total Lines**: 608 lines.

---

## 2. Declarations in `HomeFragment.kt` with Line Ranges

- **Class Declaration**:
  - `class HomeFragment : Fragment(), ClipListAdapter.Listener, ClipActionBottomSheet.Callback`: lines 41–607
- **Properties & Field Initializers**:
  - `_binding`: line 43
  - `binding`: line 44
  - `repository`: line 46
  - `categoryRepository`: line 47
  - `adapter`: line 48
  - `searchQuery`: line 50
  - `selectedCategory`: line 51
  - `selectedCategoryFilterId`: line 52
  - `collectJob`: line 53
  - `latestClips`: line 54
  - `createDocumentLauncher`: lines 56–81
- **Methods / Functions**:
  - `onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View`: lines 83–91
  - `onViewCreated(view: View, savedInstanceState: Bundle?)`: lines 93–125
    - Anonymous `androidx.core.view.MenuProvider` object: lines 110–121
      - `onCreateMenu(menu: android.view.Menu, menuInflater: android.view.MenuInflater)`: lines 111–113
      - `onMenuItemSelected(menuItem: android.view.MenuItem): Boolean`: lines 114–120
  - `setupRecyclerView()`: lines 127–131
  - `setupSwipeGestures()`: lines 133–234
    - Anonymous `ItemTouchHelper.SimpleCallback` object: lines 134–231
      - `getSwipeDirs(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int`: lines 135–140
      - `onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean`: lines 143–147
      - `onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int)`: lines 149–183
      - `onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean)`: lines 185–230
  - `setupSearch()`: lines 236–245
    - Anonymous `TextWatcher` object: lines 237–244
      - `beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int)`: line 238
      - `onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int)`: lines 239–242
      - `afterTextChanged(s: Editable?)`: line 243
  - `setupCategoryChips()`: lines 247–261
  - `observeCategories()`: lines 263–295
    - Anonymous `AdapterView.OnItemSelectedListener` object: lines 285–291
      - `onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long)`: lines 286–289
      - `onNothingSelected(parent: android.widget.AdapterView<*>?)`: line 290
  - `setupCaptureButton()`: lines 297–321
  - `setupNotificationToggle()`: lines 323–336
  - `setupFab()`: lines 338–347
  - `setupSelectionBar()`: lines 349–416
  - `observeClips()`: lines 418–434
  - `renderClips(rawClips: List<ClipEntity>)`: lines 436–453
  - `exportTxt()`: lines 455–463
  - `clearUnpinned()`: lines 465–470
  - `showMessage(msg: String)`: lines 472–475
  - `shareText(context: Context, text: String)`: lines 477–483
  - `onClipClick(clip: ClipEntity)`: lines 485–491
  - `onClipLongClick(clip: ClipEntity)`: lines 493–494
  - `onSelectionChanged(selectedCount: Int)`: lines 496–506
  - `onCopyClick(clip: ClipEntity)`: lines 508–511
  - `onPinClick(clip: ClipEntity)`: lines 513–519
  - `onFavoriteClick(clip: ClipEntity)`: lines 521–526
  - `onShareClick(clip: ClipEntity)`: lines 528–530
  - `onDeleteClick(clip: ClipEntity)`: lines 532–537
  - `onSaveNewClip(text: String)`: lines 539–556
  - `onUpdateClip(clip: ClipEntity, newContent: String)`: lines 558–568
  - `onUpdateClipCategory(clip: ClipEntity, categoryId: Long, tags: String)`: lines 570–575
  - `onShareClip(clip: ClipEntity)`: lines 577–579
  - `onCopyClip(clip: ClipEntity)`: lines 581–584
  - `onSplitClip(clip: ClipEntity, parts: List<String>, deleteOriginal: Boolean)`: lines 586–601
  - `onDestroyView()`: lines 603–606

---

## 3. References to "HomeFragment" in `app/src/main/java`
Exact output of `grep -rn "HomeFragment" app/src/main/java`:
```
app/src/main/java/com/inscopelabs/abx/clipinbox/ui/HomeFragment.kt:41:class HomeFragment : Fragment(), ClipListAdapter.Listener, ClipActionBottomSheet.Callback {
app/src/main/java/com/inscopelabs/abx/clipinbox/ui/HomeFragment.kt:74:                    Logger.e("HomeFragment", "Export failed", e)
app/src/main/java/com/inscopelabs/abx/clipinbox/ui/HomeFragment.kt:88:        Logger.d("HomeFragment", "onCreateView")
app/src/main/java/com/inscopelabs/abx/clipinbox/ui/HomeFragment.kt:95:        Logger.d("HomeFragment", "onViewCreated")
app/src/main/java/com/inscopelabs/abx/clipinbox/ui/HomeFragment.kt:157:                        Logger.i("HomeFragment", "Clip ${clip.id} archived via swipe right")
app/src/main/java/com/inscopelabs/abx/clipinbox/ui/HomeFragment.kt:163:                                    Logger.i("HomeFragment", "Clip ${clip.id} restored from archive")
app/src/main/java/com/inscopelabs/abx/clipinbox/ui/HomeFragment.kt:171:                        Logger.i("HomeFragment", "Clip ${clip.id} deleted via swipe left")
app/src/main/java/com/inscopelabs/abx/clipinbox/ui/HomeFragment.kt:177:                                    Logger.i("HomeFragment", "Clip ${clip.id} restored from deletion")
app/src/main/java/com/inscopelabs/abx/clipinbox/ui/HomeFragment.kt:258:            Logger.d("HomeFragment", "Selected category filter: $selectedCategory")
app/src/main/java/com/inscopelabs/abx/clipinbox/ui/MainActivity.kt:49:                .replace(R.id.fragment_container, HomeFragment())
app/src/main/java/com/inscopelabs/abx/clipinbox/ui/MainActivity.kt:56:                    0 -> HomeFragment()
app/src/main/java/com/inscopelabs/abx/clipinbox/ui/MainActivity.kt:59:                    else -> HomeFragment()
```

---

## 4. `MainActivity.kt` Usage of `HomeFragment`
Exact instantiation lines in `app/src/main/java/com/inscopelabs/abx/clipinbox/ui/MainActivity.kt`:

Line 49:
```kotlin
                .replace(R.id.fragment_container, HomeFragment())
```

Line 56:
```kotlin
                    0 -> HomeFragment()
```

Line 59:
```kotlin
                    else -> HomeFragment()
```

---

## 5. List of Imports in `HomeFragment.kt` & Cross-Check Against `issues/pending/`

### Complete Imports List:
1. `import android.content.Context`
2. `import android.content.Intent`
3. `import android.graphics.Canvas`
4. `import android.graphics.drawable.ColorDrawable`
5. `import android.net.Uri`
6. `import android.os.Bundle`
7. `import android.text.Editable`
8. `import android.text.TextWatcher`
9. `import android.view.LayoutInflater`
10. `import android.view.View`
11. `import android.view.ViewGroup`
12. `import androidx.activity.result.contract.ActivityResultContracts`
13. `import androidx.appcompat.app.AlertDialog`
14. `import androidx.core.content.ContextCompat`
15. `import androidx.core.view.isVisible`
16. `import androidx.fragment.app.Fragment`
17. `import androidx.lifecycle.Lifecycle`
18. `import androidx.lifecycle.lifecycleScope`
19. `import androidx.lifecycle.repeatOnLifecycle`
20. `import androidx.recyclerview.widget.ItemTouchHelper`
21. `import androidx.recyclerview.widget.LinearLayoutManager`
22. `import androidx.recyclerview.widget.RecyclerView`
23. `import com.inscopelabs.abx.clipinbox.ClipInBoxApplication`
24. `import com.inscopelabs.abx.clipinbox.R`
25. `import com.inscopelabs.abx.clipinbox.data.local.ClipEntity`
26. `import com.inscopelabs.abx.clipinbox.databinding.FragmentHomeBinding`
27. `import com.inscopelabs.abx.clipinbox.diagnostics.Logger`
28. `import com.inscopelabs.abx.clipinbox.domain.ClipRepository`
29. `import com.inscopelabs.abx.clipinbox.export.FileExporter`
30. `import com.inscopelabs.abx.clipinbox.utils.ClipboardHelper`
31. `import com.inscopelabs.abx.clipinbox.utils.NotificationPreferences`
32. `import com.google.android.material.snackbar.Snackbar`
33. `import kotlinx.coroutines.Job`
34. `import kotlinx.coroutines.flow.collectLatest`
35. `import kotlinx.coroutines.launch`
36. `import com.inscopelabs.abx.clipinbox.category.CategoryRepository`

### Cross-Check Matches Against `issues/pending/`:
- `com.inscopelabs.abx.clipinbox.export.FileExporter`
  - Matches: `issues/pending/app_src_main_java_com_inscopelabs_abx_clipinbox_export_FileExporter.kt__LOGGING-GAP.md`
- `com.inscopelabs.abx.clipinbox.utils.ClipboardHelper`
  - Matches: `issues/pending/app_src_main_java_com_inscopelabs_abx_clipinbox_utils_ClipboardHelper.kt__LOGGING-GAP.md`
- `com.inscopelabs.abx.clipinbox.utils.NotificationPreferences`
  - Matches: `issues/pending/app_src_main_java_com_inscopelabs_abx_clipinbox_utils_NotificationPreferences.kt__LOGGING-GAP.md`

*(Informational inline class references in `HomeFragment.kt` body:)*
- `SaveToPathBottomSheet` (`com.inscopelabs.abx.clipinbox.ui.SaveToPathBottomSheet`) -> `issues/pending/app_src_main_java_com_inscopelabs_abx_clipinbox_ui_SaveToPathBottomSheet.kt__LOGGING-GAP.md`
- `ClipJoiner` (`com.inscopelabs.abx.clipinbox.utility.ClipJoiner`) -> `issues/pending/app_src_main_java_com_inscopelabs_abx_clipinbox_utility_ClipJoiner.kt__LOGGING-GAP.md`

---

## 6. Verification of `HomeFragment.kt__FILE-SIZE.md`
File `issues/pending/app_src_main_java_com_inscopelabs_abx_clipinbox_ui_HomeFragment.kt__FILE-SIZE.md` exists.

### Verbatim Content:
```markdown
# FILE-SIZE Issue: HomeFragment.kt

- **File Path**: `app/src/main/java/com/inscopelabs/abx/clipinbox/ui/HomeFragment.kt`
- **Issue Type**: FILE-SIZE
- **Reason**: File is currently 607 lines, exceeding the 300-line hard threshold in AGENTS.md Section 4.1. Per Section 4.1, this file must not be included in the scope of any non-restructuring task. A dedicated restructuring task (per Section 4.2) is required before any other work touches this file.
- **Date Flagged**: 2026-08-09
- **Source**: Repository line-count audit, dated 2026-08-09. Proactive backfill.
```

---

## 7. Files Created / Touched
1. `/agent-reports/2026-08-10T03-28-30Z-homefragment-restructure-audit.md` - Created mandatory process report containing the full pre-restructuring audit.

---

## Errors / Partial Failures
- `git status` command output container index entry format incompatibility error (`fatal: unknown index entry format 0xefbf0000`).
