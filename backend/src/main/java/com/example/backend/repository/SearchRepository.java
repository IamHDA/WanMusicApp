package com.example.backend.repository;

import com.example.backend.Enum.FriendStatus;
import com.example.backend.dto.PageResponse;
import com.example.backend.dto.SearchRequestDTO;
import com.example.backend.dto.SearchResponseDTO;
import com.example.backend.dto.album.AlbumPreviewDTO;
import com.example.backend.dto.track.TrackPreviewDTO;
import com.example.backend.dto.user.ArtistProfilePreviewDTO;
import com.example.backend.dto.user.MemberProfilePreviewDTO;
import com.example.backend.dto.user.UserPreviewDTO;
import com.example.backend.entity.*;
import com.example.backend.mapper.AlbumMapper;
import com.example.backend.mapper.ArtistProfileMapper;
import com.example.backend.mapper.MemberMapper;
import com.example.backend.mapper.TrackMapper;
import com.example.backend.service.AuthenticationService;
import com.example.backend.util.FriendUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.util.*;
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
    private final FriendUtil friendUtil;

    public SearchResponseDTO search(SearchRequestDTO searchRequestDTO) {
        String keyword = searchRequestDTO.keyword();
        String type = searchRequestDTO.type();
        int pageSize = searchRequestDTO.pageSize();
        int pageNumber = searchRequestDTO.pageNumber();
        int offset = (pageNumber - 1) * pageSize;

        List<ArtistProfilePreviewDTO> artists = new ArrayList<>();
        List<TrackPreviewDTO> tracks = new ArrayList<>();
        List<AlbumPreviewDTO> albums = new ArrayList<>();
        List<MemberProfilePreviewDTO> members = new ArrayList<>();

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

    private List<ArtistProfilePreviewDTO> searchArtists(String keyword, int pageSize, int offset) {
        String jpql = "SELECT a, " +
                "CASE WHEN COUNT(f.id) > 0 THEN true ELSE false END " +
                "FROM ArtistProfile a " +
                "LEFT JOIN Follower f ON a.id = f.artist.id AND f.follower.id = :currentUserId " +
                "WHERE LOWER(a.stageName) LIKE LOWER(:keyword) " +
                "AND a.member.id <> :currentUserId " +
                "AND a.status = 'VERIFIED' " +
                "GROUP BY a " +
                "ORDER BY CASE " +
                "  WHEN LOWER(a.stageName) LIKE LOWER(:prefixKeyword) THEN 0 " +
                "  ELSE 1 " +
                "END, a.stageName ASC";

        TypedQuery<Object[]> query = entityManager.createQuery(jpql, Object[].class);
        query.setParameter("keyword", "%" + keyword + "%");
        query.setParameter("prefixKeyword", keyword + "%");
        query.setParameter("currentUserId", authenticationService.getCurrentMemberId());
        query.setFirstResult(offset);
        query.setMaxResults(pageSize);

        return query.getResultList().stream()
                .map(result -> {
                    ArtistProfile a = (ArtistProfile) result[0];
                    boolean isFollowed = (boolean) result[1];
                    ArtistProfilePreviewDTO userPreviewDTO = artistProfileMapper.toPreviewDTO(a);
                    userPreviewDTO.setFollowed(isFollowed);
                    return userPreviewDTO;
                })
                .collect(Collectors.toList());
    }

    private Long searchArtistCount(String keyword) {
        String jpql = "SELECT COUNT(DISTINCT a.id) " +
                "FROM ArtistProfile a " +
                "WHERE LOWER(a.stageName) LIKE LOWER(:keyword) " +
                "AND a.member.id <> :currentUserId " +
                "AND a.status = 'VERIFIED'";

        TypedQuery<Long> query = entityManager.createQuery(jpql, Long.class);
        query.setParameter("keyword", "%" + keyword + "%");
        query.setParameter("currentUserId", authenticationService.getCurrentMemberId());
        return query.getSingleResult();
    }

    private List<TrackPreviewDTO> searchTracks(String keyword, int pageSize, int offset) {
        String jpql = "SELECT DISTINCT t FROM Track t " +
                "LEFT JOIN FETCH t.contributions c " +
                "LEFT JOIN FETCH c.contributor " +
                "WHERE LOWER(t.title) LIKE LOWER(:keyword) " +
                "AND t.status = 'PUBLISHED' " +
                "ORDER BY CASE " +
                "   WHEN LOWER(t.title) LIKE LOWER(:prefixKeyword) THEN 0 " +
                "   ELSE 1 " +
                "END, t.title ASC";


        TypedQuery<Track> query = entityManager.createQuery(jpql, Track.class);
        query.setParameter("keyword", "%" + keyword + "%");
        query.setParameter("prefixKeyword", keyword + "%");
        query.setFirstResult(offset);
        query.setMaxResults(pageSize);

        return query.getResultList().stream()
                .map(trackMapper::toTrackPreviewDTO)
                .collect(Collectors.toList());
    }

    private Long searchTrackCount(String keyword) {
        String jpql = "SELECT COUNT(DISTINCT t.id) " +
                "FROM Track t " +
                "WHERE LOWER(t.title) LIKE LOWER(:keyword) " +
                "AND t.status = 'PUBLISHED'";

        TypedQuery<Long> query = entityManager.createQuery(jpql, Long.class);
        query.setParameter("keyword", "%" + keyword + "%");
        return query.getSingleResult();
    }

    private List<AlbumPreviewDTO> searchAlbums(String keyword, int pageSize, int offset) {
        String jpql = "SELECT DISTINCT a FROM Album a " +
                "WHERE LOWER(a.title) LIKE LOWER(:keyword) " +
                "ORDER BY CASE " +
                "   WHEN LOWER(a.title) LIKE LOWER(:prefixKeyword) THEN 0 " +
                "   ELSE 1 " +
                "END, a.title ASC";
        TypedQuery<Album> query = entityManager.createQuery(jpql, Album.class);
        query.setParameter("keyword", "%" + keyword + "%");
        query.setParameter("prefixKeyword", keyword + "%");
        query.setFirstResult(offset);
        query.setMaxResults(pageSize);

        return query.getResultList().stream()
                .map(albumMapper::toAlbumPreviewDTO)
                .collect(Collectors.toList());
    }

    private Long searchAlbumCount(String keyword) {
        String jpql = "SELECT COUNT(DISTINCT a.id) " +
                "FROM Album a " +
                "WHERE LOWER(a.title) LIKE LOWER(:keyword)";

        TypedQuery<Long> query = entityManager.createQuery(jpql, Long.class);
        query.setParameter("keyword", "%" + keyword + "%");
        return query.getSingleResult();
    }

    private List<MemberProfilePreviewDTO> searchMembers(String keyword, int pageSize, int offset) {
        String jpql =
                "SELECT m, f " +
                        "FROM Member m " +
                        "LEFT JOIN Friendship f ON (" +
                        "   (f.member.id = m.id AND f.friend.id = :currentUserId) OR " +
                        "   (f.friend.id = m.id AND f.member.id = :currentUserId)" +
                        ") " +
                        "WHERE LOWER(m.fullName) LIKE LOWER(:keyword) " +
                        "AND m.id <> :currentUserId " +
                        "ORDER BY CASE " +
                        "   WHEN LOWER(m.fullName) LIKE LOWER(:prefixKeyword) THEN 0 " +
                        "   ELSE 1 " +
                        "END ASC, " +
                        "CASE " +
                        "   WHEN f IS NULL THEN 1 " +
                        "   WHEN f.status = 'PENDING' THEN 2 " +
                        "   WHEN f.status = 'ACCEPTED' THEN 3 " +
                        "   ELSE 0 " +
                        "END DESC, " +
                        "m.fullName ASC";
        TypedQuery<Object[]> query = entityManager.createQuery(jpql, Object[].class);
        Long currentUserId = authenticationService.getCurrentMemberId();

        query.setParameter("keyword", "%" + keyword + "%");
        query.setParameter("prefixKeyword", keyword + "%");
        query.setParameter("currentUserId", currentUserId);
        query.setFirstResult(offset);
        query.setMaxResults(pageSize);

//        Set<Long> includedMemberIds = new HashSet<>();
//        List<MemberProfilePreviewDTO> memberProfilePreviewDTOS = new ArrayList<>();
//
//        query.getResultList().forEach(result -> {
//            Member member = (Member) result[0];
//            Friendship friendship = result[1] != null ? (Friendship) result[1] : null;
//
//            MemberProfilePreviewDTO memberProfilePreviewDTO = memberMapper.toPreviewDTO(member);
//            memberProfilePreviewDTO.setFriendStatus(friendUtil.getFriendshipStatus(friendship, currentUserId));
//
//            if(!includedMemberIds.contains(member.getId()))
//                memberProfilePreviewDTOS.add(memberProfilePreviewDTO);
//
//            includedMemberIds.add(member.getId());
//        });
//
//        return memberProfilePreviewDTOS;

        return query.getResultList()
                .stream()
                .map(result -> {
                    Member member = (Member) result[0];
                    Friendship friendship = result[1] != null ? (Friendship) result[1] : null;

                    System.out.println("MemberId: " + member.getId() + " Friendship: " + friendship);

                    MemberProfilePreviewDTO memberProfilePreviewDTO = memberMapper.toPreviewDTO(member);
                    memberProfilePreviewDTO.setFriendStatus(friendUtil.getFriendshipStatus(friendship, currentUserId));

                    return memberProfilePreviewDTO;
                })
                .toList();
    }

    private Long searchMemberCount(String keyword) {
        String jpql =
                "SELECT COUNT(DISTINCT m.id) " +
                        "FROM Member m " +
                        "LEFT JOIN Friendship f ON (" +
                        "   (f.member.id = m.id AND f.friend.id = :currentUserId) OR " +
                        "   (f.friend.id = m.id AND f.member.id = :currentUserId)" +
                        ") " +
                        "WHERE LOWER(m.fullName) LIKE LOWER(:keyword) " +
                        "AND m.id <> :currentUserId";

        TypedQuery<Long> query = entityManager.createQuery(jpql, Long.class);
        query.setParameter("keyword", "%" + keyword + "%");
        query.setParameter("currentUserId", authenticationService.getCurrentMemberId());
        return query.getSingleResult();
    }

    public Page<MemberProfilePreviewDTO> searchFriends(String query, int pageNumber) {
        String jpql1 =
                "SELECT CASE " +
                        "   WHEN fr.member.id = :currentUserId THEN fr.friend " +
                        "   ELSE fr.member " +
                        "END " +
                        "FROM Friendship fr " +
                        "WHERE LOWER(fr.friend.fullName) LIKE LOWER(:query) " +
                        "   OR LOWER(fr.member.fullName) LIKE LOWER(:query) " +
                        "ORDER BY CASE " +
                        "   WHEN LOWER(CASE " +
                        "       WHEN fr.member.id = :currentUserId THEN fr.friend.fullName " +
                        "       ELSE fr.member.fullName " +
                        "   END) LIKE LOWER(:prefixQuery) THEN 0 " +
                        "   ELSE 1 " +
                        "END ASC, " +
                        "CASE " +
                        "   WHEN fr.member.id = :currentUserId THEN fr.friend.fullName " +
                        "   ELSE fr.member.fullName " +
                        "END ASC";

        Long currentUserId = authenticationService.getCurrentMemberId();

        TypedQuery<Member> fetch = entityManager.createQuery(jpql1, Member.class);
        fetch.setParameter("query", "%" + query + "%");
        fetch.setParameter("prefixQuery", query + "%");
        fetch.setParameter("currentUserId", currentUserId);
        fetch.setFirstResult((pageNumber - 1) * 5);
        fetch.setMaxResults(5);

        List<MemberProfilePreviewDTO> content = fetch.getResultList().stream()
                .map(member -> {
                    MemberProfilePreviewDTO memberProfilePreviewDTO = memberMapper.toPreviewDTO(member);
                    memberProfilePreviewDTO.setFriendStatus("ACCEPTED");
                    return memberProfilePreviewDTO;
                })
                .toList();

        String countJpql =
                "SELECT COUNT(DISTINCT fr.id) " +
                        "FROM Friendship fr " +
                        "WHERE LOWER(CASE " +
                        "   WHEN fr.member.id = :currentUserId THEN fr.friend.fullName " +
                        "   ELSE fr.member.fullName " +
                        "END) LIKE LOWER(:query)";

        TypedQuery<Long> countQuery = entityManager.createQuery(countJpql, Long.class);
        countQuery.setParameter("query", "%" + query + "%");
        countQuery.setParameter("currentUserId", currentUserId);

        Long total = countQuery.getSingleResult();

        return new PageImpl<>(content, PageRequest.of(pageNumber, 5), total);
    }
}
