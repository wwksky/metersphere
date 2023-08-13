package io.metersphere.plugin.sdk.api;

public abstract class AbstractMsPlugin implements MsPlugin {

    private static final String SCRIPT_DIR = "script";

    /**
     * @return 返回默认的前端配置文件的目录
     * 可以重写定制
     */
    @Override
    public String getScriptDir() {
        return SCRIPT_DIR;
    }

    @Override
    public String getName() {
        return getKey();
    }

    @Override
    public String getPluginId() {
        return getKey().toLowerCase() + "-" + getVersion();
    }
}