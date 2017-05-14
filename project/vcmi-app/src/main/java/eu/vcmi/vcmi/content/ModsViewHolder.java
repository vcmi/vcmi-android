package eu.vcmi.vcmi.content;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import eu.vcmi.vcmi.R;

/**
 * @author F
 */
public class ModsViewHolder extends RecyclerView.ViewHolder
{
    final TextView mModNesting;
    final TextView mModName;
    final TextView mModAuthor;
    final TextView mModType;
    final TextView mModSize;
    final ImageView mStatusIcon;
    final View mDownloadBtn;

    ModsViewHolder(final View parentView)
    {
        super(LayoutInflater.from(parentView.getContext()).inflate(R.layout.mods_adapter_item, (ViewGroup) parentView, false));
        mModNesting = (TextView) itemView.findViewById(R.id.mods_adapter_item_nesting);
        mModName = (TextView) itemView.findViewById(R.id.mods_adapter_item_name);
        mModAuthor = (TextView) itemView.findViewById(R.id.mods_adapter_item_author);
        mModType = (TextView) itemView.findViewById(R.id.mods_adapter_item_modtype);
        mModSize = (TextView) itemView.findViewById(R.id.mods_adapter_item_size);
        mDownloadBtn = itemView.findViewById(R.id.mods_adapter_item_btn_download);
        mStatusIcon = (ImageView) itemView.findViewById(R.id.mods_adapter_item_status);
    }
}
