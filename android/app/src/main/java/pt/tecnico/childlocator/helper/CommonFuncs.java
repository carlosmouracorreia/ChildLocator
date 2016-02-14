package pt.tecnico.childlocator.helper;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by carloscorreia on 25/11/15.
 */
public final class CommonFuncs {

    public static boolean isEmailValid(String email) {
        final String EMAIL_PATTERN =
                "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                        + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
        Pattern pattern = Pattern.compile(EMAIL_PATTERN);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    public static boolean isPasswordValid(String password) {

        final String PASSWORD_PATTERN =
        "(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$";

        Pattern pattern = Pattern.compile(PASSWORD_PATTERN);
        Matcher matcher = pattern.matcher(password);
        return matcher.matches();
    }



    public static boolean isNameValid(String name) {

        final String NAME_PATTERN = "[A-Z][a-z]+( [A-Z][a-z]+)?";
        Pattern pattern = Pattern.compile(NAME_PATTERN);
        Matcher matcher = pattern.matcher(name);
        return matcher.matches();
    }

    public static String getPwdHash(String password,String salt) {
        try {
            String temp = password + salt;
            MessageDigest md = MessageDigest.getInstance("SHA-256");

            md.update(temp.getBytes("UTF-8")); // Change this to "UTF-16" if needed
            byte[] digest = md.digest();
            return String.format("%0" + (digest.length * 2) + 'x', new BigInteger(1, digest));
        } catch(NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch(UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

}
