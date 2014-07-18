package javax.jmdns.impl;
/*
 * @author Hua Yuanbin
 * @version v1.0
 * 
 */
public class JmdnsException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 8354105813077069108L;

    public JmdnsException(String message) {
        super(message);
    }

    public JmdnsException(Exception e) {
        super(e);
    }

}
