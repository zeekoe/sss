package nl.ronaldteune.sss.controller;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.table.TableUtils;
import nl.ronaldteune.sss.model.DBAlbum;

import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class Database {

    private static List<DBAlbum> dbAlbums;
    private JdbcConnectionSource connectionSource;
    private final Dao<DBAlbum, String> albumDao;

    public Database() {
        try {
            connectionSource = new JdbcConnectionSource("jdbc:sqlite:db.sqlite");
            TableUtils.createTableIfNotExists(connectionSource, DBAlbum.class);
            albumDao = DaoManager.createDao(connectionSource, DBAlbum.class);
        } catch(SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void init(String rootPath) throws SQLException {
        getAllAlbumsFromDB();
        final List<DBAlbum> fsAlbums = Filesystem.getAlbums(dbAlbums, rootPath);
        for(DBAlbum album : fsAlbums) {
            System.out.print(album.getUrl() + ": ");
            final int result = albumDao.create(album);
            System.out.println(result);
        }
        Database.dbAlbums.clear();
        getAllAlbumsFromDB();
    }

    public List<DBAlbum> getAllAlbums() {
        return dbAlbums;
    }

    private void getAllAlbumsFromDB() throws SQLException {
        dbAlbums = albumDao
                .queryForAll()
                .stream()
                .sorted(Comparator.comparing(DBAlbum::getArtist))
                .collect(Collectors.toList());
    }

    public void updateAlbum(DBAlbum dbAlbum) throws SQLException {
        albumDao.update(dbAlbum);
    }
}
