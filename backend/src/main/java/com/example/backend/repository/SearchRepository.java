package com.example.backend.repository;

import com.example.backend.dto.PageResponse;
import com.example.backend.dto.SearchRequestDTO;
import com.example.backend.dto.SearchResponseDTO;
import com.example.backend.dto.album.AlbumPreviewDTO;
import com.example.backend.dto.track.TrackPreviewDTO;
import com.example.backend.dto.user.UserPreviewDTO;
import com.example.backend.entity.*;
import com.example.backend.mapper.AlbumMapper;
import com.example.backend.mapper.ArtistProfileMapper;
import com.example.backend.mapper.MemberMapper;
import com.example.backend.mapper.TrackMapper;
import com.example.backend.service.AuthenticationService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class SearchRepository {

    private final EntityManager entityManager;
    private final AuthenticationService authenticationService;
    private final TrackMapper trackMapper;
    private final ArtistProfileMapper artistProfileMapper;
    private final MemberMapper memberMapper;
    private final AlbumMapper albumMapper;

    public SearchResponseDTO search(SearchRequestDTO searchRequestDTO) {
        String keyword = searchRequestDTO.keyword();
        String type = searchRequestDTO.type();
        int pageSize = searchRequestDTO.pageSize();
        int pageNumber = searchRequestDTO.pageNumber();
        int offset = (pageNumber - 1) * pageSize;

        List<UserPreviewDTO> artists = new ArrayList<>();
        List<TrackPreviewDTO> tracks = new ArrayList<>();
        List<AlbumPreviewDTO> albums = new ArrayList<>();
        List<UserPreviewDTO> members = new ArrayList<>();

        SearchResponseDTO searchResponseDTO = new SearchResponseDTO();

        // Search based on type
        if (type == null || type.equals("all") || type.equals("artists")) {
            artists = searchArtists(keyword, pageSize, offset);
            long totalItem = searchArtistCount(keyword);
            int totalPage = (int) Math.ceil((double) totalItem / offset);
            searchResponseDTO.setArtistPreviewDTOS(new PageResponse<>(
                    artists,
                    pageNumber,
                    pageSize,
                    totalItem,
                    totalPage
            ));
        }

        if (type == null || type.equals("all") || type.equals("tracks")) {
            tracks = searchTracks(keyword, pageSize, offset);
            long totalItem = searchTrackCount(keyword);
            int totalPage = (int) Math.ceil((double) totalItem / offset);
            searchResponseDTO.setTrackPreviewDTOS(new PageResponse<>(
                    tracks,
                    pageNumber,
                    pageSize,
                    totalItem,
                    totalPage
            ));
        }

        if (type == null || type.equals("all") || type.equals("albums")) {
            albums = searchAlbums(keyword, pageSize, offset);
            long totalItem = searchAlbumCount(keyword);
            int totalPage = (int) Math.ceil((double) totalItem / offset);
            searchResponseDTO.setAlbumPreviewDTOS(new PageResponse<>(
                    albums,
                    pageNumber,
                    pageSize,
                    totalItem,
                    totalPage
            ));
        }

        if (type == null || type.equals("all") || type.equals("members")) {
            members = searchMembers(keyword, pageSize, offset);
            long totalItem = searchMemberCount(keyword);
            int totalPage = (int) Math.ceil((double) totalItem / offset);
            searchResponseDTO.setMemberPreviewDTOS(new PageResponse<>(
                    members,
                    pageNumber,
                    pageSize,
                    totalItem,
                    totalPage
            ));
        }

        return searchResponseDTO;
    }

    private List<UserPreviewDTO> searchArtists(String keyword, int pageSize, int offset) {
        String jpql = "SELECT a, " +
                "CASE WHEN COUNT(f.id) > 0 THEN true ELSE false END " +
                "FROM ArtistProfile a " +
                "LEFT JOIN Follower f ON a.id = f.artist.id AND f.follower.id = :currentUserId " +
                "WHERE LOWER(a.stageName) LIKE LOWER(:keyword) AND a.id <> :currentUserId " +
                "GROUP BY a";

        TypedQuery<Object[]> query = entityManager.createQuery(jpql, Object[].class);
        query.setParameter("keyword", "%" + keyword + "%");
        query.setParameter("currentUserId", authenticationService.getCurrentMemberId());
        query.setFirstResult(offset);
        query.setMaxResults(pageSize);

        return query.getResultList().stream()
                .map(result -> {
                    ArtistProfile a = (ArtistProfile) result[0];
                    boolean isFollowed = (boolean) result[1];
                    UserPreviewDTO userPreviewDTO = artistProfileMapper.toPreviewDTO(a);
                    userPreviewDTO.setFollowed(isFollowed);
                    userPreviewDTO.setFriend(false);
                    return userPreviewDTO;
                })
                .collect(Collectors.toList());
    }

    private Long searchArtistCount(String keyword){
        String jpql = "SELECT COUNT(DISTINCT a) " +
                "FROM ArtistProfile a " +
                "LEFT JOIN Follower f ON a.id = f.artist.id AND f.follower.id = :currentUserId " +
                "WHERE LOWER(a.stageName) LIKE LOWER(:keyword) AND a.id <> :currentUserId ";

        TypedQuery<Long> query = entityManager.createQuery(jpql, Long.class);
        query.setParameter("keyword", "%" + keyword + "%");
        query.setParameter("currentUserId", authenticationService.getCurrentMemberId());
        return query.getSingleResult();
    }

    private List<TrackPreviewDTO> searchTracks(String keyword, int pageSize, int offset) {
        String jpql = "SELECT DISTINCT t FROM Track t " +
                "LEFT JOIN FETCH t.contributions c " +
                "LEFT JOIN FETCH c.contributor " +
                "WHERE LOWER(t.title) LIKE LOWER(:keyword)";

        TypedQuery<Track> query = entityManager.createQuery(jpql, Track.class);
        query.setParameter("keyword", "%" + keyword + "%");
        query.setFirstResult(offset);
        query.setMaxResults(pageSize);

        return query.getResultList().stream()
                .map(trackMapper::toTrackPreviewDTO)
                .collect(Collectors.toList());
    }

    private Long searchTrackCount(String keyword){
        String jpql = "SELECT COUNT(DISTINCT t) FROM Track t WHERE LOWER(t.title) LIKE LOWER(:keyword)";
        TypedQuery<Long> query = entityManager.createQuery(jpql, Long.class);
        query.setParameter("keyword", "%" + keyword + "%");
        return query.getSingleResult();
    }

    private List<AlbumPreviewDTO> searchAlbums(String keyword, int pageSize, int offset) {
        String jpql = "SELECT DISTINCT a FROM Album a WHERE LOWER(a.title) LIKE LOWER(:keyword)";
        TypedQuery<Album> query = entityManager.createQuery(jpql, Album.class);
        query.setParameter("keyword", "%" + keyword + "%");
        query.setFirstResult(offset);
        query.setMaxResults(pageSize);

        return query.getResultList().stream()
                .map(albumMapper::toAlbumPreviewDTO)
                .collect(Collectors.toList());
    }

    private Long searchAlbumCount(String keyword){
        String jpql = "SELECT COUNT(DISTINCT a) FROM Album a WHERE LOWER(a.title) LIKE LOWER(:keyword)";
        TypedQuery<Long> query = entityManager.createQuery(jpql, Long.class);
        query.setParameter("keyword", "%" + keyword + "%");
        return query.getSingleResult();
    }

    private List<UserPreviewDTO> searchMembers(String keyword, int pageSize, int offset) {
        String jpql = "SELECT m, " +
                "CASE WHEN COUNT(f.id) > 0 THEN true ELSE false END " +
                " FROM Member m " +
                "LEFT JOIN FETCH Friendship f ON f.friend.id = :currentUserId " +
                "OR f.member.id = :currentUserId " +
                "WHERE LOWER(m.fullName) LIKE LOWER(:keyword) AND m.id <> :currentUserId " +
                "GROUP BY m";
        TypedQuery<Object[]> query = entityManager.createQuery(jpql, Object[].class);
        query.setParameter("keyword", "%" + keyword + "%");
        query.setParameter("currentUserId", authenticationService.getCurrentMemberId());
        query.setFirstResult(offset);
        query.setMaxResults(pageSize);

        return query.getResultList().stream()
                .map(result -> {
                    Member a = (Member) result[0];
                    boolean isFriend = (boolean) result[1];
                    UserPreviewDTO userPreviewDTO = memberMapper.toPreviewDTO(a);
                    userPreviewDTO.setFollowed(false);
                    userPreviewDTO.setFriend(isFriend);
                    return userPreviewDTO;
                })
                .collect(Collectors.toList());
    }

    private Long searchMemberCount(String keyword){
        String jpql = "SELECT COUNT(DISTINCT m) FROM Member m " +
                "WHERE LOWER(m.fullName) LIKE LOWER(:keyword) AND m.id <> :currentUserId";
        TypedQuery<Long> query = entityManager.createQuery(jpql, Long.class);
        query.setParameter("keyword", "%" + keyword + "%");
        query.setParameter("currentUserId", authenticationService.getCurrentMemberId());
        return query.getSingleResult();
    }

    public Page<UserPreviewDTO> searchFriends(String query, int pageNumber) {
        String jpql1 = "SELECT CASE WHEN fr.member.id = :currentUserId THEN fr.friend ELSE fr.member END " +
                "FROM Friendship fr WHERE LOWER(fr.friend.fullName) " +
                "LIKE LOWER(:query) OR LOWER(fr.member.fullName) LIKE LOWER(:query)";

        TypedQuery<Member> fetch = entityManager.createQuery(jpql1, Member.class);
        fetch.setParameter("query", "%" + query + "%");
        fetch.setParameter("currentUserId", authenticationService.getCurrentMemberId());
        fetch.setFirstResult((pageNumber - 1) * 5);
        fetch.setMaxResults(5);

        List<UserPreviewDTO> content = fetch.getResultList().stream()
                .map(member -> {
                    UserPreviewDTO userPreviewDTO = memberMapper.toPreviewDTO(member);
                    userPreviewDTO.setFollowed(false);
                    userPreviewDTO.setFriend(true);
                    return userPreviewDTO;
                })
                .toList();

        String jpql2 = "SELECT COUNT(fr) FROM Friendship fr WHERE LOWER(fr.friend.fullName) " +
                "LIKE LOWER(:query) OR LOWER(fr.member.fullName) LIKE LOWER(:query)";

        TypedQuery<Long> count = entityManager.createQuery(jpql2, Long.class);
        Long totalItems = count.getSingleResult();

        return new PageImpl<>(content, PageRequest.of(pageNumber, 5), totalItems);
    }
}
