package eu.vcmi.vcmi.settings;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;

import eu.vcmi.vcmi.R;
import eu.vcmi.vcmi.util.FileUtil;
import eu.vcmi.vcmi.util.SharedPrefs;

/**
 * @author F
 */
public class PointerModeSettingController extends LauncherSettingWithDialogController<PointerModeSettingController.PointerMode, DoubleConfig>
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
        mConfig.mPrefs.saveEnum(SharedPrefs.KEY_POINTER_MODE, item);
        mConfig.mConfig.updateSwipe(item.supportsSwipe());
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
            PointerModeSettingDialog.pointerModeToUserString(mActivity, mConfig.mPrefs.loadEnum(SharedPrefs.KEY_POINTER_MODE, PointerMode.NORMAL)));
    }

    public enum PointerMode
    {
        NORMAL,
        NORMAL_WITH_SWIPE,
        RELATIVE;

        public boolean supportsSwipe()
        {
            return this == NORMAL_WITH_SWIPE;
        }
    }
}
