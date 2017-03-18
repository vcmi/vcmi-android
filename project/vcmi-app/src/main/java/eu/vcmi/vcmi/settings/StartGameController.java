package eu.vcmi.vcmi.settings;

import android.support.v7.app.AppCompatActivity;
import android.view.View;

import eu.vcmi.vcmi.R;

/**
 * @author F
 */
public class StartGameController extends LauncherSettingController<Void, Void>
{
    private View.OnClickListener mOnSelectedAction;

    public StartGameController(final AppCompatActivity act, final View.OnClickListener onSelectedAction)
    {
        super(act);
        mOnSelectedAction = onSelectedAction;
    }

    @Override
    protected String mainText()
    {
        return mActivity.getString(R.string.launcher_btn_start_title);
    }

    @Override
    protected String subText()
    {
        // TODO reference vcmi version dynamically instead of hardcoding (or obtain it on build time)
        return mActivity.getString(R.string.launcher_btn_start_subtitle);
    }

    @Override
    public void onClick(final View v)
    {
        mOnSelectedAction.onClick(v);
    }
}
