package model;

public class Driver {

    private final int id;
    private final String name;
    private final String email;
    private final String password;

    public Driver(int id, String name, String email, String password) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password != null ? password : "";
    }

    public int getId()          { return id; }
    public String getName()     { return name; }
    public String getEmail()    { return email; }
    public String getPassword() { return password; }

    @Override
    public String toString() {
        return "Fahrer[name=" + name + ", email=" + email + "]";
    }
}
