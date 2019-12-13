package nl.ronaldteune.sss.controller;

import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.probe.FFmpegFormat;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;
import nl.ronaldteune.sss.cache.Cache;
import nl.ronaldteune.sss.model.Artist;
import nl.ronaldteune.sss.model.Child;
import nl.ronaldteune.sss.model.DBAlbum;
import nl.ronaldteune.sss.model.Directory;
import nl.ronaldteune.sss.model.Index;
import nl.ronaldteune.sss.model.Indexes;
import spark.Request;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static nl.ronaldteune.sss.SuperSimpleSonic.MUSIC_PATH;

public class ArtistAlbum {
    private Cache<Directory> directoryCache = new Cache<>();

    public Indexes getArtistList(Database database) {
        List<DBAlbum> dbAlbums = database.getAllAlbums();

        List<Artist> artistList = new ArrayList<>();
        String lastArtist = "";
        for (DBAlbum album : dbAlbums) {
            if (!lastArtist.equals(album.getArtist())) {
                artistList.add(new Artist("ARTIST-" + album.getId(), album.getArtist()));
            }
            lastArtist = album.getArtist();
        }

        return new Indexes(Collections.singletonList(new Index("A", artistList)));
    }

    public Directory getAlbumsOrTracks(Request req, Database database) {
        List<DBAlbum> allAlbums = database.getAllAlbums();
        final Directory directory;
        final String queryId = req.queryParams("id").replace("ARTIST-", "");
        if (req.queryParams("id").startsWith("ARTIST")) {
            directory = buildAlbumList(allAlbums, queryId);
        } else {
            directory = buildTrackList(allAlbums, queryId);
        }
        return directory;
    }

    private Directory buildTrackList(List<DBAlbum> allAlbums, String albumId) {
        return directoryCache.get("buildTrackList" + albumId, () -> {
            Directory directory = new Directory();
            DBAlbum album = getAlbumFromId(allAlbums, albumId);
            directory.setId(album.getIdString());
            directory.setParent("ARTIST-" + album.getIdString());
            directory.setName(album.getTitle());
            try (Stream<Path> paths = Files.walk(getPathFromAlbum(album))) {
                final List<Path> songs = paths
                        .sorted(Comparator.comparing(p -> p.getFileName().toString()))
                        .collect(Collectors.toList());
                for (int songId = 0; songId < songs.size(); songId++) {
                    Path song = songs.get(songId);
                    if (song.getFileName().toString().endsWith("mp3")
                            || song.getFileName().toString().endsWith("ogg")
                            || song.getFileName().toString().endsWith("flac")) {

                        final String songTitleFromPath = song.toString().replace(getPathFromAlbum(album).toAbsolutePath().toString() + "/", "");
                        FFprobe probe = new FFprobe("/usr/bin/ffprobe");
                        final FFmpegProbeResult probeResult = probe.probe(song.toString());
                        final FFmpegFormat format = probeResult.getFormat();

                        Child child = new Child();
                        child.setId(album.getIdString() + "-" + songId);
                        child.setParent(albumId);
                        child.setAlbum(album.getTitle());
                        child.setArtist(album.getArtist());
                        if (format.tags == null) {
                            try {
                                child.setTrack("" + Integer.parseInt(songTitleFromPath.split("-")[0]));
                            } catch (Exception e) {
                                child.setTrack("");
                            }
                            child.setTitle(songTitleFromPath);
                        } else {
                            child.setTrack(format.tags.containsKey("track") ? format.tags.get("track").split("/")[0] : "");
                            child.setTitle(format.tags.getOrDefault("title", song.toString().replace(MUSIC_PATH, "")));
                        }
                        child.setBitrate((int) format.bit_rate / 1000);
                        child.setDuration((int) format.duration);
                        child.setCoverArt(albumId);
                        child.setDir(false);
                        child.setPath(song.toString().replace(MUSIC_PATH, ""));
                        directory.addChild(child);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return directory;
        });
    }

    private Path getPathFromAlbum(DBAlbum album) {
        return Paths.get(MUSIC_PATH + "/" + album.getUrl());
    }

    private Directory buildAlbumList(List<DBAlbum> allAlbums, String queryId) {
        return directoryCache.get("buildAlbumList" + queryId, () -> {
            Directory directory = new Directory();
            DBAlbum artist = getAlbumFromId(allAlbums, queryId);
            List<DBAlbum> albums = allAlbums.stream().filter(a -> a.getArtist().equals(artist.getArtist())).collect(Collectors.toList());
            directory.setId(artist.getIdString());
            directory.setParent("0");
            directory.setName(artist.getArtist());
            for (DBAlbum dbAlbum : albums) {
                Child child = new Child();
                child.setId(dbAlbum.getIdString());
                child.setParent(artist.getIdString());
                child.setTitle(dbAlbum.getTitle());
                child.setArtist(dbAlbum.getArtist());
                child.setDir(true);
                child.setCoverArt(dbAlbum.getIdString());
                directory.addChild(child);
            }
            return directory;
        });
    }

    public String getStreamPath(String requestId, Database database) {
        final String[] ids = requestId.split("-");
        List<DBAlbum> allAlbums = database.getAllAlbums();
        Directory directory = buildTrackList(allAlbums, ids[0]);
        return directory.getChild().stream().filter(c -> c.getId().equals(requestId)).findAny().orElse(null).getPath();
    }

    private DBAlbum getAlbumFromId(List<DBAlbum> allAlbums, String albumId) {
        return allAlbums.stream().filter(a -> albumId.equals(a.getIdString())).findFirst().orElse(null);
    }

    public byte[] getCoverArt(String id, Database database) {
        List<DBAlbum> allAlbums = database.getAllAlbums();
        final Path albumPath = getPathFromAlbum(getAlbumFromId(allAlbums, id));
        try {
            return SoundStream.getStream(albumPath.resolve("cover.jpg").toString());
        } catch (Exception ignored) {}
        return new byte[0];
    }
}
