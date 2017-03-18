package eu.vcmi.vcmi.settings;

import android.support.v7.app.AppCompatActivity;

import java.io.File;

import eu.vcmi.vcmi.Config;
import eu.vcmi.vcmi.R;
import eu.vcmi.vcmi.util.FileUtil;

/**
 * @author F
 */
public class CodepageSettingController extends LauncherSettingWithDialogController<String, Config>
{
    public CodepageSettingController(final AppCompatActivity activity)
    {
        super(activity);
    }

    @Override
    protected LauncherSettingDialog<String> dialog()
    {
        return new CodepageSettingDialog();
    }

    @Override
    public void onItemChosen(final String item)
    {
        mConfig.mCodepage = item;
        mConfig.save(new File(FileUtil.configFileLocation()));
        updateContent();
    }

    @Override
    protected String mainText()
    {
        return mActivity.getString(R.string.launcher_btn_cp_title);
    }

    @Override
    protected String subText()
    {
        if (mConfig == null)
        {
            return "";
        }
        return mConfig.mCodepage == null || mConfig.mCodepage.isEmpty()
               ? mActivity.getString(R.string.launcher_btn_cp_subtitle_unknown)
               : mActivity.getString(R.string.launcher_btn_cp_subtitle, mConfig.mCodepage);
    }
}
