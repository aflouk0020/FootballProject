package ie.tus.oop1.football.util;

/** Unchecked exception for wrapping SQL issues in DAOs when you want cleaner signatures. */
public class DataAccessRuntimeException extends RuntimeException 
{
    public DataAccessRuntimeException(String message, Throwable cause) 
    { 
    	super(message, cause);
    }
}


