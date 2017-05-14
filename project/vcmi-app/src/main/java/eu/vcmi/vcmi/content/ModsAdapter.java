package eu.vcmi.vcmi.content;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;

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
    private final List<ModItem> mDataset = new ArrayList<>();
    private final IOnItemAction mItemListener;

    public ModsAdapter(final List<ModItem> mods, final IOnItemAction itemListener)
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
        final ModItem item = mDataset.get(position);
        final Context ctx = holder.itemView.getContext();
        holder.mModName.setText(item.mMod.mName + ", " + item.mMod.mVersion);
        holder.mModType.setText(item.mMod.mModType);
        if (item.mMod.mSize > 0)
        {
            holder.mModSize.setVisibility(View.VISIBLE);
            holder.mModSize.setText(String.format(Locale.getDefault(), "%.1f kB", item.mMod.mSize / 1024.0f)); // TODO unit conversion
        }
        else
        {
            holder.mModSize.setVisibility(View.GONE);
        }
        holder.mModAuthor.setText(ctx.getString(R.string.mods_item_author_template, item.mMod.mAuthor));
        holder.mStatusIcon.setImageResource(selectModStatusIcon(item.mMod.mActive));

        if (item.mNestingLevel > 0)
        {
            holder.mModNesting.setText(String.format("%" + (item.mNestingLevel + 1) + "s", ">").replace(' ', '-'));
        }
        else
        {
            holder.mModNesting.setText("");
        }

        holder.mDownloadBtn.setVisibility(View.GONE); // TODO visible for mods that aren't downloaded

        holder.itemView.setOnClickListener(v -> mItemListener.onItemPressed(item, holder));
        holder.mStatusIcon.setOnClickListener(v -> mItemListener.onTogglePressed(item, holder));
        holder.mDownloadBtn.setOnClickListener(v -> mItemListener.onDownloadPressed(item, holder));
    }

    private int selectModStatusIcon(final boolean active)
    {
        // TODO distinguishing mods that aren't downloaded or have an update available
        if (active)
        {
            return R.drawable.ic_star_full;
        }
        return R.drawable.ic_star_empty;
    }

    @Override
    public int getItemCount()
    {
        return mDataset.size();
    }

    public void attachSubmods(final ModItem mod, final RecyclerView.ViewHolder vh)
    {
        int adapterPosition = vh.getAdapterPosition();
        final List<ModItem> submods = Stream.of(mod.mMod.submods()).map(v -> new ModItem(v, mod.mNestingLevel + 1)).collect(Collectors.toList());
        mDataset.addAll(adapterPosition + 1, submods);
        notifyItemRangeInserted(adapterPosition + 1, submods.size());
    }

    public void detachSubmods(final ModItem mod, final RecyclerView.ViewHolder vh)
    {
        final int adapterPosition = vh.getAdapterPosition();
        final int checkedPosition = adapterPosition + 1;
        int detachedElements = 0;
        while (checkedPosition < mDataset.size() && mDataset.get(checkedPosition).mNestingLevel > mod.mNestingLevel)
        {
            ++detachedElements;
            mDataset.remove(checkedPosition);
        }
        notifyItemRangeRemoved(checkedPosition, detachedElements);
    }

    public interface IOnItemAction
    {
        void onItemPressed(final ModItem mod, final RecyclerView.ViewHolder vh);

        void onDownloadPressed(final ModItem mod, final RecyclerView.ViewHolder vh);

        void onTogglePressed(ModItem item, ModsViewHolder holder);
    }

    public static class ModItem
    {
        public final VCMIMod mMod;
        public int mNestingLevel;
        public boolean mExpanded;

        public ModItem(final VCMIMod mod)
        {
            this(mod, 0);
        }

        public ModItem(final VCMIMod mod, final int nestingLevel)
        {
            mMod = mod;
            mNestingLevel = nestingLevel;
            mExpanded = false;
        }

        @Override
        public String toString()
        {
            return mMod.toString();
        }
    }
}
