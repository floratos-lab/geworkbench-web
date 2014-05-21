package org.geworkbenchweb.genspace.wrapper;

import java.security.NoSuchAlgorithmException;

import javax.xml.datatype.XMLGregorianCalendar;

import org.geworkbench.components.genspace.server.stubs.User;
import org.geworkbenchweb.genspace.ui.component.GenSpaceLogin_1;

public class UserWrapper {
	private User delegate;
	
	private GenSpaceLogin_1 login;
	
	public User getDelegate() {
		return delegate;
	}
	public UserWrapper(User delegate, GenSpaceLogin_1 login) {
		this.delegate = delegate;
		this.login = login;
	}
	public String getIdstr() {
		return delegate.getIdstr();
	}
	public void setIdstr(String value) {
		delegate.setIdstr(value);
	}
	public Object getRef() {
		return delegate.getRef();
	}
	public void setRef(Object value) {
		delegate.setRef(value);
	}
	public XMLGregorianCalendar getCreatedAt() {
		return delegate.getCreatedAt();
	}
	public void setCreatedAt(XMLGregorianCalendar value) {
		delegate.setCreatedAt(value);
	}
	public String getFakeId() {
		return delegate.getFakeId();
	}
	public void setFakeId(String value) {
		delegate.setFakeId(value);
	}
	public int getOnlineStatus() {
		return delegate.getOnlineStatus();
	}
	public void setOnlineStatus(int value) {
		delegate.setOnlineStatus(value);
	}
	public String getPassword() {
		return delegate.getPassword();
	}
	public void setPassword(String value) {
		delegate.setPassword(value);
	}
	public String getPhone() {
		String phone;
		
		try {
			phone = delegate.getPhone();
		} catch(Exception e) {
			phone = "";
		}

		return phone;
	}
	public void setPhone(String value) {
		delegate.setPhone(value);
	}
	public String getState() {
		String state;
		
		try {
			state = delegate.getState();
		} catch (Exception e) {
			state = "";
		}
		
		return state;
	}
	public void setState(String value) {
		delegate.setState(value);
	}
	public String getUsername() {
		String username;
		
		try {
			username = delegate.getUsername();
		} catch (Exception e) {
			username = "";
		}
		
		return username;
	}
	public void setUsername(String value) {
		delegate.setUsername(value);
	}
	public boolean isVisible() {
		return delegate.isVisible();
	}
	public void setVisible(boolean value) {
		delegate.setVisible(value);
	}
	public String getWorkTitle() {
		String worktitle;
		
		try {
			worktitle = delegate.getWorkTitle();
		} catch (Exception e) {
			worktitle = "";
		}
		
		return worktitle;
	}
	public void setWorkTitle(String value) {
		delegate.setWorkTitle(value);
	}
	public String toString() {
		return delegate.toString();
	}
	public String getAddr1() {
		String addr1;
		
		try {
			addr1 = delegate.getAddr1();
		} catch (Exception e) {
			addr1 = "";
		}
		
		return addr1;
	}
	public void setAddr1(String addr1) {
		delegate.setAddr1(addr1);
	}
	public String getAddr2() {
		String addr2;
		
		try {
			addr2 = delegate.getAddr2();
		} catch (Exception e) {
			addr2 = "";
		}
		
		return addr2;
	}
	public void setAddr2(String addr2) {
		delegate.setAddr2(addr2);
	}
	public String getCity() {
		String city;
		
		try {
			city = delegate.getCity();
		} catch (Exception e) {
			city = "";
		}
		
		return city;
	}
	public void setCity(String city) {
		delegate.setCity(city);
	}

	public int getDataVisibility() {
		return delegate.getDataVisibility();
	}
	public void setDataVisibility(int dataVisibility) {
		delegate.setDataVisibility(dataVisibility);
	}
	public String getEmail() {
		String email;
		
		try {
			email = delegate.getEmail();
		} catch (Exception e) {
			email = "";
		}
		
		return email;
	}
	public void setEmail(String email) {
		delegate.setEmail(email);
	}
	public String getFirstName() {
		String firstName;
		try {
			firstName = delegate.getFirstName();
		} catch (Exception e) {
			firstName = "";
		}
		
		return firstName;
	}
	public void setFirstName(String firstName) {
		delegate.setFirstName(firstName);
	}

	public int getId() {
		return delegate.getId();
	}
	public void setId(int id) {
		delegate.setId(id);
	}

	public String getInterests() {
		String interests;
		
		try {
			interests = delegate.getInterests();
		} catch (Exception e) {
			interests = "";
		}

		return interests;
	}
	public void setInterests(String interests) {
		delegate.setInterests(interests);
	}
	public String getLabAffiliation() {
		String affiliation;
		
		try {
			affiliation = delegate.getLabAffiliation();
		} catch (Exception e) {
			affiliation = "";
		}
		
		return affiliation;
	}
	public void setLabAffiliation(String labAffiliation) {
		delegate.setLabAffiliation(labAffiliation);
	}
	public String getLastName() {
		String lastName;
		
		try {
			lastName = delegate.getLastName();
		} catch (Exception e) {
			lastName = "";
		}
		
		return lastName;
	}
	public void setLastName(String lastName) {
		delegate.setLastName(lastName);
	}
	public int getLogData() {
		return delegate.getLogData();
	}
	public void setLogData(int logData) {
		delegate.setLogData(logData);
	}
	
	public String getZipcode() {
		String zip;
		
		try {
			zip = delegate.getZipcode();
		} catch (Exception e) {
			zip = "";
		}
		
		return zip;
	}
	public void setZipcode(String zipcode) {
		delegate.setZipcode(zipcode);
	}
	
	public String getFullName() {		
		return this.getFirstName() + " " + this.getLastName();
	}

	private String na(String s)
	{
		return (s == null || s.length() == 0 ? "N/A" : s);
	}
	public String getFullNameWUsername()
	{
		if(getFirstName().length() == 0 && getLastName().length() ==0)
			return getUsername();
		else
			return getFirstName() + " "
			+ getLastName() + " (" + getUsername() + ")";
	}
	
	public String toHTML() {
		String r = "<html><body><b>" +getFullNameWUsername() + "</b><br>";
		if (login.getGenSpaceServerFactory().isVisible(delegate)) {
			r += "<i>"
					+ (getWorkTitle() != null
							&& getWorkTitle() != "" ? getWorkTitle() + " at " : "")
					+ (getLabAffiliation() != null ? getLabAffiliation():  " (affiliation not disclosed)") + "</i><br><br>";
			r += "<b>Research Interests:</b><br />"
					+(getInterests() == null ? "(not disclosed)" : getInterests()) + "<br><br>";
			r += "<b>Contact information:</b><br /><br>Phone: "
					+ na(getPhone()) + "<br>Email: "
					+ na(getEmail()) + "<br><br>Mailing Address:<br>"
					+ (getAddr1().length() == 0 && getAddr2().length() == 00 && getCity().length() == 0 && getState().length() == 0 && getZipcode().length() == 0 ? "not provided" : 
						 na(getAddr1()) + "<br>" + na(getAddr2())
					+ "<br>" + na(getCity()) + ", "
					+ na(getState()) + ", " + na(getZipcode()));
		}
		else {
		
			r += "This user is not visible to you. Please add them as a friend or join one of their networks to see their profile.";
		}
		r += "</body>";
		r += "</html>";
		return r;
	}
	public String getShortName() {
		if(getFirstName() != null && !getFirstName().equals(""))
			return getFirstName();
		return getUsername();
	}
//	public List<User> getFriendsProfiles() {
//		ArrayList<User> ret = new ArrayList<User>();
//		
//		for(Friend f: delegate.getFriends())
//			ret.add(f.getRightUser());
//		return ret;
//	}
//	public boolean containsFolderByName(String folderName) {
//		for(WorkflowFolder f : getFolders())
//			if(f.getName().equals(folderName))
//				return true;
//		return false;
//	}
	public void setPasswordClearText(String password)
	{
		delegate.setPassword(getEncryptedPassword(password.toCharArray()));

	}
	protected final static String HEX_DIGITS = "0123456789abcdef";
//public static void main(String[] args) {
	//System.out.println(getEncryptedPassword("test123".toCharArray()));
//}
	public static String getEncryptedPassword(char[] c_password) {
		String plaintext = new String(c_password);
		
		java.security.MessageDigest d =null;
				try {
					d = java.security.MessageDigest.getInstance("SHA-1");
				} catch (NoSuchAlgorithmException e) {
//					GenSpaceServerFactory.logger.error("Error",e);
				}
				d.reset();
				d.update(plaintext.getBytes());
				byte[] hashedBytes =  d.digest();
				StringBuffer sb = new StringBuffer(hashedBytes.length * 2);
		        for (int i = 0; i < hashedBytes.length; i++) {
		             int b = hashedBytes[i] & 0xFF;
		             sb.append(HEX_DIGITS.charAt(b >>> 4)).append(HEX_DIGITS.charAt(b & 0xF));
		        }
		        return sb.toString();	
	}
//	public User loadVisibility(User from) {
//		setVisible(isVisibleTo(from));
//		return delegate;
//	}

	public boolean isFriendsWith() {
		return delegate.isFriendsWith();
	}
	
	public void setFriendsWith(boolean friendsWith) {
		delegate.setFriendsWith(friendsWith);
	}
	@Override
	public int hashCode() {
		return this.getId();
	}
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof User)
		{
			return ((User) obj).getId() == this.getId();
		}
		else if(obj instanceof UserWrapper)
		{
			return ((UserWrapper) obj).getId() == this.getId();
		}
		return false;
	}
	public int compareTo(UserWrapper o) {
		int r = this.getLastName().compareTo(o.getLastName());
		if(r == 0)
			return this.getFirstName().compareTo(o.getFirstName());
		return r;
	}
}
