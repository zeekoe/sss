package nl.ronaldteune.sss.controller;

import nl.ronaldteune.sss.model.DBAlbum;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Filesystem {
    static List<DBAlbum> getAlbums(List<DBAlbum> dbAlbums, String rootPath) {
        List<DBAlbum> fsAlbums = new ArrayList<>();
        final List<String> artistAlbums = dbAlbums.stream().map(a -> a.getArtist() + "/" + a.getTitle()).collect(Collectors.toList());
        try (Stream<Path> paths =
             Files.walk(Paths.get(rootPath), 2, FileVisitOption.FOLLOW_LINKS)) {
            final List<Path> artistAlbumPaths = paths
                    .filter(p -> isNotYetInAlbumList(artistAlbums, p))
                    .filter(p -> isSecondLevelPath(rootPath, p))
                    .filter(p -> Files.isDirectory(p))
                    .collect(Collectors.toList());
            int i = dbAlbums.stream().map(DBAlbum::getId).max(Integer::compareTo).orElse(1);
            System.out.println("Getting " + artistAlbumPaths.size() + " additional albums from disk. This may take a while.");
            System.out.println("Starting at offset " + i);
            for(Path p : artistAlbumPaths) {
                final String artist = getArtist(p);
                final String album = getAlbum(p);
                final DBAlbum t = new DBAlbum(i++, artist + "/" + album, artist, album);
                fsAlbums.add(t);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Finished, running...");
        return fsAlbums;
    }

    private static boolean isNotYetInAlbumList(List<String> artistAlbums, Path p) {
        return !artistAlbums.contains(getArtist(p) + "/" + getAlbum(p));
    }

    private static boolean isSecondLevelPath(String rootPath, Path p) {
        return p.getParent().getParent().toString().equals(rootPath);
    }

    private static String getAlbum(Path p) {
        return p.getName(p.getNameCount() - 1).toString();
    }

    private static String getArtist(Path p) {
        return p.getName(p.getNameCount() - 2).toString();
    }
}
