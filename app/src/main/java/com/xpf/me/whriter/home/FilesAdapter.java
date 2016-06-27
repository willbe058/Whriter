package com.xpf.me.whriter.home;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.mr5.icarus.entity.Image;
import com.xpf.me.whriter.R;
import com.xpf.me.whriter.model.WhriterFile;

import java.util.ArrayList;
import java.util.List;

import io.realm.OrderedRealmCollection;
import io.realm.RealmList;
import io.realm.RealmRecyclerViewAdapter;
import tk.zielony.naturaldateformat.AbsoluteDateFormat;
import tk.zielony.naturaldateformat.NaturalDateFormat;
import tk.zielony.naturaldateformat.RelativeDateFormat;

/**
 * Created by pengfeixie on 16/5/27.
 */
public class FilesAdapter extends RealmRecyclerViewAdapter<WhriterFile, FilesAdapter.FileHolder> {

    private Context mContext;
    private OnFolderClickListener onFolderClickListener;
    private OnFileClickListener onFileClickListener;
    private OnItemLongClickListener onItemLongClickListener;

    public FilesAdapter(Context context, OrderedRealmCollection<WhriterFile> files) {
        super(context, files, true);
        this.mContext = context;
    }

    public void add(WhriterFile file, int position) {
        getData().add(position, file);
        notifyItemInserted(position);
    }

    public void setOnFolderClickListener(OnFolderClickListener listener) {
        this.onFolderClickListener = listener;
    }

    public void setOnFileClickListener(OnFileClickListener onFileClickListener) {
        this.onFileClickListener = onFileClickListener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
        this.onItemLongClickListener = onItemLongClickListener;
    }

    @Override
    public FileHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View root = LayoutInflater.from(mContext).inflate(R.layout.item_article, parent, false);
        return new FileHolder(root);
    }

    @Override
    public void onBindViewHolder(FileHolder holder, int position) {
        WhriterFile item = getData().get(position);
        holder.itemView.setTag(item);
        holder.fileTypeImg.setImageResource(item.isFile() ? R.drawable.ic_doc : R.drawable.ic_folde);
        holder.preview.setVisibility(item.isFile() ? View.VISIBLE : View.GONE);
        holder.title.setText(item.getTitle());

        if (item.isFile()) {
            holder.preview.setText(item.getPreview());
        } else {
            holder.preview.setVisibility(View.GONE);
        }

        if (System.currentTimeMillis() - item.getModifyDate() > 6 * 1000 * 60 * 60) {
            AbsoluteDateFormat absoluteDateFormat = new AbsoluteDateFormat(context,
                    NaturalDateFormat.DATE | NaturalDateFormat.HOURS | NaturalDateFormat.MINUTES);
            holder.createDate.setText(absoluteDateFormat.format(item.getModifyDate()));
        } else {
            RelativeDateFormat relativeDateFormat = new RelativeDateFormat(context, NaturalDateFormat.TIME);
            holder.createDate.setText(relativeDateFormat.format(item.getModifyDate()));
        }
    }

    public class FileHolder extends RecyclerView.ViewHolder {

        public ImageView fileTypeImg;
        public TextView title, preview, createDate;

        public FileHolder(View itemView) {
            super(itemView);
            fileTypeImg = (ImageView) itemView.findViewById(R.id.img_file_type);
            title = (TextView) itemView.findViewById(R.id.tx_title);
            preview = (TextView) itemView.findViewById(R.id.tx_preview);
            createDate = (TextView) itemView.findViewById(R.id.create_date);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    WhriterFile item = (WhriterFile) v.getTag();
                    if (!item.isFile()) {
                        if (onFolderClickListener != null) {
                            onFolderClickListener.onClick(item);
                        }
                    } else {
                        if (onFileClickListener != null) {
                            onFileClickListener.onClick(item);
                        }
                    }
                }
            });
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (onItemLongClickListener != null) {
                        onItemLongClickListener.onLongClick(((WhriterFile) v.getTag()));
                    }
                    return true;
                }
            });
        }
    }

    public interface OnFolderClickListener {
        void onClick(WhriterFile file);
    }

    public interface OnFileClickListener {
        void onClick(WhriterFile file);
    }

    public interface OnItemLongClickListener {
        void onLongClick(WhriterFile file);
    }


}
