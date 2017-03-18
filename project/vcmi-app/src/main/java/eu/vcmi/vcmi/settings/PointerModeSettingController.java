package eu.vcmi.vcmi.settings;

import android.support.v7.app.AppCompatActivity;

import eu.vcmi.vcmi.R;
import eu.vcmi.vcmi.util.SharedPrefs;

/**
 * @author F
 */
public class PointerModeSettingController extends LauncherSettingWithDialogController<PointerModeSettingController.PointerMode, SharedPrefs>
{
    public PointerModeSettingController(final AppCompatActivity activity)
    {
        super(activity);
    }

    @Override
    protected LauncherSettingDialog<PointerMode> dialog()
    {
        return new PointerModeSettingDialog();
    }

    @Override
    public void onItemChosen(final PointerMode item)
    {
        mConfig.saveEnum(SharedPrefs.KEY_POINTER_MODE, item);
        updateContent();
    }

    @Override
    protected String mainText()
    {
        return mActivity.getString(R.string.launcher_btn_pointermode_title);
    }

    @Override
    protected String subText()
    {
        if (mConfig == null)
        {
            return "";
        }
        return mActivity.getString(R.string.launcher_btn_pointermode_subtitle,
            PointerModeSettingDialog.pointerModeToUserString(mActivity, mConfig.loadEnum(SharedPrefs.KEY_POINTER_MODE, PointerMode.NORMAL)));
    }

    public enum PointerMode
    {
        NORMAL,
        RELATIVE
    }
}
