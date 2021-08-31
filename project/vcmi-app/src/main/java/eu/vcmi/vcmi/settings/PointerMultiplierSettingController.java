package eu.vcmi.vcmi.settings;

import androidx.appcompat.app.AppCompatActivity;

import eu.vcmi.vcmi.R;
import eu.vcmi.vcmi.util.SharedPrefs;

/**
 * @author F
 */
public class PointerMultiplierSettingController extends LauncherSettingWithDialogController<Float, SharedPrefs>
{
    public PointerMultiplierSettingController(final AppCompatActivity activity)
    {
        super(activity);
    }

    @Override
    protected LauncherSettingDialog<Float> dialog()
    {
        return new PointerMultiplierSettingDialog();
    }

    @Override
    public void onItemChosen(final Float item)
    {
        mConfig.save(SharedPrefs.KEY_POINTER_MULTIPLIER, item);
        updateContent();
    }

    @Override
    protected String mainText()
    {
        return mActivity.getString(R.string.launcher_btn_pointermulti_title);
    }

    @Override
    protected String subText()
    {
        if (mConfig == null)
        {
            return "";
        }
        return mActivity.getString(R.string.launcher_btn_pointermulti_subtitle,
            PointerMultiplierSettingDialog.pointerMultiplierToUserString(mConfig.load(SharedPrefs.KEY_POINTER_MULTIPLIER, 1.0f)));
    }
}
