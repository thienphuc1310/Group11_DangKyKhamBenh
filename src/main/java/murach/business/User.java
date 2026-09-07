//User, UserDB, TheEmailServlet

package murach.business;

//implements Serializable chỉ nên được dùng khi muốn
//đóng gói và chuyển sang nơi khác
import java.io.Serializable;

public class User implements Serializable
{
    private String firstName;
    private String lastName;
    private String email;

    //Constructor không tham số
    public User()
    {
        firstName = "";
        lastName = "";
        email = "";
    }

    //Constructor 3 tham số
    public User (String firstName, String lastName, String email)
    {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }

    //getter firstName
    public String getFirstName()
    {
        return firstName;
    }
    //setter firstName
    public void setFirstName(String firstName)
    {
        this.firstName = firstName;
    }

    //getter lastName
    public String getLastName()
    {
        return lastName;
    }
    //setter lastName
    public void setLastName(String lastName)
    {
        this.lastName = lastName;
    }

    //getter email
    public String getEmail()
    {
        return email;
    }
    //setter email
    public void setEmail(String email)
    {
        this.email = email;
    }
}