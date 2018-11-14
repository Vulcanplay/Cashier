package icar.a5i4s.com.cashierb.module;

/**
 * Created by light on 2016/10/18.
 */
public class User {
    private int id;
    private String userId;
    private String roleName;
    private String account;
    private String password;

    public User(int id, String userId, String roleName, String account, String password) {
        this.id = id;
        this.userId = userId;
        this.roleName = roleName;
        this.account = account;
        this.password = password;
    }
    public User(String userId, String roleName, String account, String password) {
        this.userId = userId;
        this.roleName = roleName;
        this.account = account;
        this.password = password;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
