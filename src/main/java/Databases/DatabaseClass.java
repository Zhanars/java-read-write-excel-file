package Databases;

public class DatabaseClass implements Interfaces.Database {
    public String gethost;
    private String getuser;
    private String getpassword;
    private String getdbtype;
    private String getdbname;

    public DatabaseClass(String gethost, String getuser, String getpassword, String getdbtype, String getdbname) {
        this.gethost = gethost;
        this.getuser = getuser;
        this.getpassword = getpassword;
        this.getdbtype = getdbtype;
        this.getdbname = getdbname;
    }

    @Override
    public String gethost() {
        return gethost;
    }

    @Override
    public String getuser() {
        return getuser;
    }

    @Override
    public String getpassword() {
        return getpassword;
    }



    @Override
    public String getdbtype() {
        return getdbtype;
    }

    @Override
    public String getdbname() {
        return getdbname;
    }

    public void setGethost(String gethost) {
        this.gethost = gethost;
    }

    public void setGetuser(String getuser) {
        this.getuser = getuser;
    }

    public void setGetpassword(String getpassword) {
        this.getpassword = getpassword;
    }


    public void setGetdbtype(String getdbtype) {
        this.getdbtype = getdbtype;
    }

    public void setGetdbname(String getdbname) {
        this.getdbname = getdbname;
    }
}
