package eu.vcmi.vcmi.settings;

import java.util.ArrayList;
import java.util.List;

import eu.vcmi.vcmi.R;

/**
 * @author F
 */
public class ScreenResSettingDialog extends LauncherSettingDialog<ScreenResSettingController.ScreenRes>
{
    private static final List<ScreenResSettingController.ScreenRes> AVAILABLE_RESOLUTIONS = new ArrayList<>();

    static
    {
        AVAILABLE_RESOLUTIONS.add(new ScreenResSettingController.ScreenRes(800, 600));
        AVAILABLE_RESOLUTIONS.add(new ScreenResSettingController.ScreenRes(1024, 600));
        AVAILABLE_RESOLUTIONS.add(new ScreenResSettingController.ScreenRes(1024, 768));
        AVAILABLE_RESOLUTIONS.add(new ScreenResSettingController.ScreenRes(1280, 800));
        AVAILABLE_RESOLUTIONS.add(new ScreenResSettingController.ScreenRes(1280, 960));
        AVAILABLE_RESOLUTIONS.add(new ScreenResSettingController.ScreenRes(1280, 1024));
        AVAILABLE_RESOLUTIONS.add(new ScreenResSettingController.ScreenRes(1366, 768));
        AVAILABLE_RESOLUTIONS.add(new ScreenResSettingController.ScreenRes(1440, 900));
        AVAILABLE_RESOLUTIONS.add(new ScreenResSettingController.ScreenRes(1600, 1200));
        AVAILABLE_RESOLUTIONS.add(new ScreenResSettingController.ScreenRes(1680, 1050));
        AVAILABLE_RESOLUTIONS.add(new ScreenResSettingController.ScreenRes(1920, 1080));
    }

    public ScreenResSettingDialog()
    {
        super(AVAILABLE_RESOLUTIONS);
    }

    @Override
    protected int dialogTitleResId()
    {
        return R.string.launcher_btn_res_title;
    }

    @Override
    protected CharSequence itemName(final ScreenResSettingController.ScreenRes item)
    {
        return item.toString();
    }
}
