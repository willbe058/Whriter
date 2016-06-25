package com.xpf.me.whriter.home;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.xpf.me.whriter.R;
import com.xpf.me.whriter.common.AppData;
import com.xpf.me.whriter.common.RealmProvider;
import com.xpf.me.whriter.editor.EditorActivity;
import com.xpf.me.whriter.event.BusProvider;
import com.xpf.me.whriter.model.WhriterFile;
import com.yarolegovich.lovelydialog.LovelyTextInputDialog;

import java.util.UUID;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * Created by pengfeixie on 16/5/27.
 */
public class FilesFragment extends Fragment {

    private static final String TAG = FilesFragment.class.getName();

    private ContentLoadingProgressBar progressBar;
    private FloatingActionMenu floatingActionMenu;
    private FloatingActionButton fileFab, folderFab;
    private RecyclerView recyclerView;
    private FilesAdapter adapter;
    private View emptyView;
    private ImageView emptyFolderView;
    private View coorLayout;
    private View longClickView;
    private View deleteButton, renameButton;

    private WhriterFile mCurrentFolder;
    private RealmResults<WhriterFile> currentFolderResults;
    private WhriterFile mChosenFile;
    private boolean backEnable;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_filelist, container, false);
        progressBar = (ContentLoadingProgressBar) root.findViewById(R.id.progress);
        recyclerView = (RecyclerView) root.findViewById(R.id.recyclerView);
        fileFab = (FloatingActionButton) root.findViewById(R.id.add_file);
        folderFab = (FloatingActionButton) root.findViewById(R.id.add_folder);
        floatingActionMenu = (FloatingActionMenu) root.findViewById(R.id.menu);
        emptyView = root.findViewById(R.id.empty_view);
        emptyFolderView = (ImageView) root.findViewById(R.id.empty_folder);
        coorLayout = root.findViewById(R.id.list_container);
        longClickView = root.findViewById(R.id.view_long_click);
        deleteButton = root.findViewById(R.id.delete_container);
        renameButton = root.findViewById(R.id.rename_container);

        longClickView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {

                generateAnimator(v).start();
            }
        });
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mChosenFile != null) {
                    RealmProvider.getInstance().getRealm().executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            mChosenFile.deleteFromRealm();
                            generateAnimator(longClickView).start();
                        }
                    });
                }
            }
        });
        renameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mChosenFile != null) {
                    generateAnimator(longClickView).start();
                    RealmProvider.getInstance().getRealm().executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            new LovelyTextInputDialog(getActivity(), R.style.EditTextDialogTheme)
                                    .setTitle(AppData.getString(R.string.rename_title))
                                    .setInitialInput(mChosenFile.getTitle())
                                    .setIcon(R.drawable.ic_mode_edit_white_24dp)
                                    .setTopColorRes(R.color.colorFab)
                                    .setConfirmButton(android.R.string.ok, new LovelyTextInputDialog.OnTextInputConfirmListener() {
                                        @Override
                                        public void onTextInputConfirmed(final String text) {
                                            RealmProvider
                                                    .getInstance()
                                                    .getRealm()
                                                    .executeTransaction(new Realm.Transaction() {
                                                        @Override
                                                        public void execute(Realm realm) {
                                                            mChosenFile.setTitle(text);
                                                            realm.copyToRealm(mChosenFile);
                                                        }
                                                    });
                                        }
                                    })
                                    .show();

                        }
                    });
                }
            }
        });

        floatingActionMenu.setClosedOnTouchOutside(true);
        RealmResults<WhriterFile> realmResults = RealmProvider.getInstance()
                .getRealm()
                .where(WhriterFile.class)
                .equalTo("isRoot", true)
                .findAllSortedAsync("createDate", Sort.DESCENDING);
        realmResults.addChangeListener(new RealmChangeListener<RealmResults<WhriterFile>>() {
            @Override
            public void onChange(RealmResults<WhriterFile> element) {
                progressBar.hide();
                emptyView.setVisibility(element.size() == 0 ? View.VISIBLE : View.GONE);
            }
        });
        adapter = new FilesAdapter(getActivity(), realmResults);
        adapter.setOnFolderClickListener(new FilesAdapter.OnFolderClickListener() {
            @Override
            public void onClick(WhriterFile file) {
                coorLayout.setTranslationY(60);
                coorLayout.setAlpha(0);
                coorLayout.animate().translationY(0).setDuration(500);
                coorLayout.animate().alpha(1).setDuration(500);
                mCurrentFolder = file;
                if (currentFolderResults != null) {
                    currentFolderResults.removeChangeListeners();
                }
                mCurrentFolder = file;
                BusProvider.getInstance().post(new BusProvider.FolderChangeEvent(mCurrentFolder));
                currentFolderResults =
                        RealmProvider.getInstance()
                                .getRealm()
                                .where(WhriterFile.class)
                                .equalTo("currentFolder.id", file.getId())
                                .findAllSortedAsync("createDate", Sort.DESCENDING);
                currentFolderResults.addChangeListener(new RealmChangeListener<RealmResults<WhriterFile>>() {
                    @Override
                    public void onChange(RealmResults<WhriterFile> element) {

                        emptyFolderView.setVisibility(element.size() == 0 ? View.VISIBLE : View.GONE);
                        adapter.updateData(element);
                    }
                });
            }
        });
        adapter.setOnFileClickListener(new FilesAdapter.OnFileClickListener() {
            @Override
            public void onClick(WhriterFile file) {
                EditorActivity.enterEditor(getActivity(), file.getId());
            }
        });
        adapter.setOnItemLongClickListener(new FilesAdapter.OnItemLongClickListener() {
            @Override
            public void onLongClick(WhriterFile file) {
                BusProvider.getInstance().post(new BusProvider.LongClickEvent(false));
                backEnable = false;
                mChosenFile = file;
                longClickView.setVisibility(View.VISIBLE);
                longClickView.setAlpha(0);
                longClickView.animate().alpha(1);
                longClickView.setTranslationY(50);
                longClickView.animate().translationY(0);
            }
        });
        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), 1, LinearLayoutManager.VERTICAL, false);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        fileFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                floatingActionMenu.close(true);
                new LovelyTextInputDialog(getActivity(), R.style.EditTextDialogTheme)
                        .setTitle(AppData.getString(R.string.title))
                        .setIcon(R.drawable.ic_mode_edit_white_24dp)
                        .setTopColorRes(R.color.colorFab)
                        .setInputFilter(AppData.getString(R.string.title_not_null), new LovelyTextInputDialog.TextFilter() {
                            @Override
                            public boolean check(String text) {
                                return !text.isEmpty();
                            }
                        })
                        .setConfirmButton(android.R.string.ok, new LovelyTextInputDialog.OnTextInputConfirmListener() {
                            @Override
                            public void onTextInputConfirmed(String text) {
                                if (mCurrentFolder != null) {
                                    RealmProvider.getInstance().getRealm().beginTransaction();
                                    mCurrentFolder.setCreateDate(System.currentTimeMillis());
                                    RealmProvider.getInstance().getRealm().commitTransaction();
                                }
                                final WhriterFile file = new WhriterFile();
                                file.setId(UUID.randomUUID().toString());
                                file.setFile(true);
                                file.setTitle(text);
                                file.setRoot(mCurrentFolder == null);
                                file.setCurrentFolder(mCurrentFolder);
                                file.setCreateDate(System.currentTimeMillis());
                                file.setPreviousFolder(mCurrentFolder == null
                                        ? null
                                        : mCurrentFolder.getCurrentFolder());
                                RealmProvider.getInstance().getRealm().executeTransaction(new Realm.Transaction() {
                                    @Override
                                    public void execute(Realm realm) {
                                        realm.copyToRealm(file);
                                    }
                                });
                            }
                        })
                        .show();
            }
        });

        folderFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                floatingActionMenu.close(true);
                new LovelyTextInputDialog(getActivity(), R.style.EditTextDialogTheme)
                        .setTitle(AppData.getString(R.string.name))
                        .setIcon(R.drawable.ic_folder_white_24dp)
                        .setTopColorRes(R.color.colorFab)
                        .setInputFilter(AppData.getString(R.string.name_not_null), new LovelyTextInputDialog.TextFilter() {
                            @Override
                            public boolean check(String text) {
                                return !text.isEmpty();
                            }
                        })
                        .setConfirmButton(android.R.string.ok, new LovelyTextInputDialog.OnTextInputConfirmListener() {
                            @Override
                            public void onTextInputConfirmed(String text) {
                                if (mCurrentFolder != null) {
                                    RealmProvider.getInstance().getRealm().beginTransaction();
                                    mCurrentFolder.setCreateDate(System.currentTimeMillis());
                                    RealmProvider.getInstance().getRealm().commitTransaction();
                                }
                                final WhriterFile file = new WhriterFile();
                                file.setId(UUID.randomUUID().toString());
                                file.setFile(false);
                                file.setRoot(mCurrentFolder == null);
                                file.setTitle(text);
                                file.setCreateDate(System.currentTimeMillis());
                                file.setCurrentFolder(mCurrentFolder);
                                file.setPreviousFolder(mCurrentFolder == null
                                        ? null
                                        : mCurrentFolder.getCurrentFolder());
                                RealmProvider.getInstance().getRealm().executeTransaction(new Realm.Transaction() {
                                    @Override
                                    public void execute(Realm realm) {
                                        realm.copyToRealm(file);
                                    }
                                });
                            }
                        })
                        .show();
            }
        });
        return root;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        progressBar.show();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BusProvider.getInstance().register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        BusProvider.getInstance().unregister(this);
        if (currentFolderResults != null) {
            currentFolderResults.removeChangeListeners();
        }
    }

    private Animator generateAnimator(final View view) {
        Animator animator = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f);
        animator.setDuration(300);
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                view.setVisibility(View.GONE);
                BusProvider.getInstance().post(new BusProvider.LongClickEvent(true));
                backEnable = true;
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        return animator;
    }

    public boolean onBackPressed() {
        if (!backEnable) {
            generateAnimator(longClickView).start();
            return true;
        }
        if (floatingActionMenu.isOpened()) {
            floatingActionMenu.close(true);
            return true;
        }
        if (mCurrentFolder == null) {
            return false;
        }
        mCurrentFolder = mCurrentFolder.getCurrentFolder();
        if (currentFolderResults != null) {
            currentFolderResults.removeChangeListeners();
        }
        if (mCurrentFolder == null) {
            BusProvider.getInstance().post(new BusProvider.FolderChangeEvent(mCurrentFolder));
            currentFolderResults =
                    RealmProvider.getInstance()
                            .getRealm()
                            .where(WhriterFile.class)
                            .equalTo("isRoot", true)
                            .findAllSortedAsync("createDate", Sort.DESCENDING);
            currentFolderResults.addChangeListener(new RealmChangeListener<RealmResults<WhriterFile>>() {
                @Override
                public void onChange(RealmResults<WhriterFile> element) {
                    emptyView.setVisibility(element.size() == 0
                            ? View.VISIBLE
                            : View.GONE);
                    emptyFolderView.setVisibility(View.GONE);
                    adapter.updateData(element);
                }
            });
            return true;
        }

        BusProvider.getInstance().post(new BusProvider.FolderChangeEvent(mCurrentFolder));
        currentFolderResults =
                RealmProvider.getInstance()
                        .getRealm()
                        .where(WhriterFile.class)
                        .equalTo("currentFolder.id", mCurrentFolder.getId())
                        .findAllSortedAsync("createDate", Sort.DESCENDING);
        currentFolderResults.addChangeListener(new RealmChangeListener<RealmResults<WhriterFile>>() {
            @Override
            public void onChange(RealmResults<WhriterFile> element) {
                emptyFolderView.setVisibility(element.size() == 0 ? View.VISIBLE : View.GONE);
                adapter.updateData(element);
            }
        });
        return true;
    }
}
