package eu.vcmi.vcmi.settings;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.annimon.stream.Stream;

import java.util.List;

import eu.vcmi.vcmi.util.Log;

/**
 * @author F
 */
public abstract class LauncherSettingDialog<T> extends DialogFragment
{
    protected final List<T> mDataset;
    private IOnItemChosen<T> mObserver;

    protected LauncherSettingDialog(final List<T> dataset)
    {
        mDataset = dataset;
    }

    public void observe(final IOnItemChosen<T> observer)
    {
        mObserver = observer;
    }

    protected abstract CharSequence itemName(T item);

    protected abstract int dialogTitleResId();

    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState)
    {
        return new AlertDialog.Builder(getActivity())
            .setTitle(dialogTitleResId())
            .setItems(Stream.of(mDataset).map(this::itemName).toArray(CharSequence[]::new), this::onItemChosenInternal)
            .create();
    }

    private void onItemChosenInternal(final DialogInterface dialog, final int index)
    {
        final T chosenItem = mDataset.get(index);
        Log.d(this, "Chosen item: " + chosenItem);
        dialog.dismiss();
        if (mObserver != null)
        {
            mObserver.onItemChosen(chosenItem);
        }
    }

    public interface IOnItemChosen<V>
    {
        void onItemChosen(V item);
    }
}
