package eu.vcmi.vcmi.content;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import eu.vcmi.vcmi.R;
import eu.vcmi.vcmi.mods.VCMIMod;

/**
 * @author F
 */
public class ModsAdapter extends RecyclerView.Adapter<ModsViewHolder>
{
    private final List<VCMIMod> mDataset = new ArrayList<>();
    private final IOnItemAction mItemListener;

    public ModsAdapter(final List<VCMIMod> mods, final IOnItemAction itemListener)
    {
        mItemListener = itemListener;
        mDataset.addAll(mods);
    }

    @Override
    public ModsViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType)
    {
        return new ModsViewHolder(parent);
    }

    @Override
    public void onBindViewHolder(final ModsViewHolder holder, final int position)
    {
        final VCMIMod item = mDataset.get(position);
        final Context ctx = holder.itemView.getContext();
        holder.mModName.setText(item.mName + ", " + item.mVersion);
        holder.mModType.setText(item.mModType);
        if (item.mSize > 0)
        {
            holder.mModSize.setVisibility(View.VISIBLE);
            holder.mModSize.setText(String.format(Locale.getDefault(), "%.1f kB", item.mSize / 1024.0f)); // TODO unit conversion
        }
        else
        {
            holder.mModSize.setVisibility(View.GONE);
        }
        holder.mModAuthor.setText(ctx.getString(R.string.mods_item_author_template, item.mAuthor));
        holder.mStatusIcon.setImageResource(selectModStatusIcon(item.mActive));

        holder.itemView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(final View v)
            {
                mItemListener.onItemPressed(item, holder);
            }
        });

        holder.mDownloadBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(final View v)
            {
                mItemListener.onDownloadPressed(item, holder);
            }
        });
    }

    private int selectModStatusIcon(final boolean active)
    {
        if (active) // TODO better icons...
        {
            return android.R.drawable.btn_star_big_on;
        }
        return android.R.drawable.btn_star_big_off;
    }

    @Override
    public int getItemCount()
    {
        return mDataset.size();
    }

    public interface IOnItemAction
    {
        void onItemPressed(final VCMIMod mod, final RecyclerView.ViewHolder vh);

        void onDownloadPressed(final VCMIMod mod, final RecyclerView.ViewHolder vh);
    }
}
