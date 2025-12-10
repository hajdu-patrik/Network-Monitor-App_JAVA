package networkmonitor.db;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import networkmonitor.model.BlacklistEntry;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BlacklistDao {
    // Logger for debugging and information
    private static final Logger LOGGER = Logger.getLogger(BlacklistDao.class.getName());

    /**
     * Saves a single BlacklistEntry to the database.
     * @param entry The BlacklistEntry to save
     */
    public void save(BlacklistEntry entry) {
        EntityManager em = DatabaseManager.getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(entry);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            LOGGER.log(Level.SEVERE, "Error saving entry to MSSQL", e);
        } finally {
            em.close();
        }
    }

    /**
     * Loads all BlacklistEntry records from the database.
     * @return List of BlacklistEntry objects
     */
    @SuppressWarnings("null")
    public List<BlacklistEntry> loadAllEntries() {
        EntityManager em = DatabaseManager.getEntityManager();
        try {
            TypedQuery<BlacklistEntry> query = em.createQuery("SELECT b FROM BlacklistEntry b", BlacklistEntry.class);
            List<BlacklistEntry> results = query.getResultList();
            
            LOGGER.log(Level.INFO, "Loaded {0} entries from MSSQL.", results.size());
            return results;
        } finally {
            em.close();
        }
    }
    
    /**
     * Saves a list of BlacklistEntry records to the database in a batch.
     * @param entries List of BlacklistEntry objects to save
     */
    public void saveAll(List<BlacklistEntry> entries) {
        if (entries == null || entries.isEmpty())
            return;

        EntityManager em = DatabaseManager.getEntityManager();
        try {
            em.getTransaction().begin();
            
            for (int i = 0; i < entries.size(); i++) {
                em.persist(entries.get(i));
                
                if (i > 0 && i % 50 == 0) {
                    em.flush();
                    em.clear();
                }
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            LOGGER.log(Level.SEVERE, "Error saving batch to MSSQL", e);
        } finally {
            em.close();
        }
    }
}