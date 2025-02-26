package service;
import dataaccess.DataAccessException;
import dataaccess.DataAccess; // Import your DataAccess interface
import dataaccess.MemoryDataAccess; // Import your in-memory implementation

public class ClearService {
    private final DataAccess dataAccess;

    public ClearService() {
        // Initialize DataAccess (for now, use in-memory)
        this.dataAccess = new MemoryDataAccess();
    }
    public ClearService(DataAccess dataAccess) {
        // Initialize DataAccess (for now, use in-memory)
        this.dataAccess = dataAccess;
    }

    public void clear() throws DataAccessException {
        dataAccess.clear();
    }

}
