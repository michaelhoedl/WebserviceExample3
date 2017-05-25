package com.example.campus02.webserviceexample3.model;

/**
 * Created by andreas on 21.05.2017.
 */

public class SyncTodoEntry {
    private int id;
    private String url;
    // mögliche Http-Cmds sind GET, POST, DELETE...
    private String cmd;
    private String headers;
    private String params;
    private String jsonPostStr;


    /**
     * Konstruktor ohne Id, da diese in der lokalen Datenbank automatisch vergeben wird.
     *
     * @param url
     * @param cmd
     * @param headers
     * @param params
     * @param jsonPostStr
     */
    public SyncTodoEntry(String url, String cmd, String headers, String params, String jsonPostStr) {
        this.url = url;
        this.cmd = cmd;
        this.headers = headers;
        this.params = params;
        this.jsonPostStr = jsonPostStr;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getCmd() {
        return cmd;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }

    public String getHeaders() {
        return headers;
    }

    public void setHeaders(String headers) {
        this.headers = headers;
    }

    public String getParams() {
        return params;
    }

    public void setParams(String params) {
        this.params = params;
    }

    public String getJsonPostStr() {
        return jsonPostStr;
    }

    public void setJsonPostStr(String jsonPostStr) {
        this.jsonPostStr = jsonPostStr;
    }

    @Override
    public String toString() {
        return "SyncTodoEntry{" +
                "id=" + id +
                ", url='" + url + '\'' +
                ", cmd='" + cmd + '\'' +
                ", headers='" + headers + '\'' +
                ", params='" + params + '\'' +
                ", jsonPostStr='" + jsonPostStr + '\'' +
                '}';
    }
}
