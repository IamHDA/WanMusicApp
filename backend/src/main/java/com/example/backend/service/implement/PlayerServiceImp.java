package com.example.backend.service.implement;

import com.example.backend.Enum.TrackStatus;
import com.example.backend.dto.PageResponse;
import com.example.backend.dto.track.TrackPreviewDTO;
import com.example.backend.entity.PlayerState;
import com.example.backend.entity.Tag;
import com.example.backend.entity.Track;
import com.example.backend.mapper.TrackMapper;
import com.example.backend.repository.*;
import com.example.backend.service.PlayerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PlayerServiceImp implements PlayerService {

    private final AuthenticationServiceImp authenticationService;
    private final PlayerStateRepository playerStateRepo;
    private final TrackTagRepository trackTagRepo;
    private final TrackRepository trackRepo;
    private final TrackMapper trackMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TrackPreviewDTO> generateQueue(int index) {
        Long currentMemberId = authenticationService.getCurrentMemberId();
        int pageSize = 10;
        List<TrackPreviewDTO> resultQueue = new ArrayList<>();

        // 1. Lấy PlayerState an toàn
        Optional<PlayerState> stateOpt = playerStateRepo.findByMemberId(currentMemberId);

        // NẾU CHƯA TỪNG NGHE GÌ (Trạng thái rỗng) -> Trả về random nhạc
        if (stateOpt.isEmpty() || stateOpt.get().getTrack() == null) {
            Page<Track> generalTracks = trackRepo.findAllByStatus(
                    TrackStatus.PUBLISHED, // Đã sửa thành PUBLISHED
                    PageRequest.of(index, pageSize)
            );
            resultQueue = generalTracks.stream().map(trackMapper::toTrackPreviewDTO).toList();
            return new PageResponse<>(resultQueue, index, generalTracks.getTotalPages());
        }

        PlayerState state = stateOpt.get();
        Track currentTrack = state.getTrack();
        List<Track> contextTracks = new ArrayList<>();

        // Lấy ngữ cảnh nếu có
        if (state.getPlaylist() != null) {
            contextTracks = state.getPlaylist().getTracks().stream().map(pt -> pt.getTrack()).toList();
        } else if (state.getAlbum() != null) {
            contextTracks = state.getAlbum().getTracks().stream().map(at -> at.getTrack()).toList();
        }

        int startOffset = index * pageSize;

        // LỚP 1: Nạp nhạc từ Playlist / Album
        if (startOffset < contextTracks.size()) {
            int endOffset = Math.min(startOffset + pageSize, contextTracks.size());
            List<Track> subList = contextTracks.subList(startOffset, endOffset);
            resultQueue.addAll(subList.stream().map(trackMapper::toTrackPreviewDTO).toList());
        }

        // Kiểm tra xem còn thiếu chỗ không (Padding)
        int remainingSlots = pageSize - resultQueue.size();

        if (remainingSlots > 0) {
            // Tạo danh sách ID để theo dõi, tránh thêm trùng bài đang phát hoặc đã có trong Queue
            List<Long> existingIds = new ArrayList<>();
            existingIds.add(currentTrack.getId());
            resultQueue.forEach(t -> existingIds.add(t.getId()));

            // LỚP 2: Đề xuất theo Thể loại (Tag) của bài hiện tại
            List<Long> currentTrackTagIds = currentTrack.getTags().stream()
                    .map(tt -> tt.getTag().getId())
                    .toList();

            if (!currentTrackTagIds.isEmpty()) {
                Pageable tagPageable = PageRequest.of(index, remainingSlots + 5);
                List<Track> recTracks = trackTagRepo.findByTag_IdIn(currentTrackTagIds, tagPageable)
                        .stream().map(tt -> tt.getTrack()).distinct().toList();

                for (Track t : recTracks) {
                    if (resultQueue.size() < pageSize && !existingIds.contains(t.getId())) {
                        resultQueue.add(trackMapper.toTrackPreviewDTO(t));
                        existingIds.add(t.getId());
                    }
                }
            }

            // LỚP 3: Lưới an toàn (Ultimate Fallback) - Quét toàn bộ kho nhạc
            remainingSlots = pageSize - resultQueue.size();
            if (remainingSlots > 0) {
                // Tái sử dụng hàm findAllByStatus với status PUBLISHED
                Page<Track> fallbackTracks = trackRepo.findAllByStatusAndNotInAlbum(
                        TrackStatus.PUBLISHED,
                        PageRequest.of(index, remainingSlots + 10)
                );

                for (Track t : fallbackTracks.getContent()) {
                    if (resultQueue.size() < pageSize && !existingIds.contains(t.getId())) {
                        resultQueue.add(trackMapper.toTrackPreviewDTO(t));
                        existingIds.add(t.getId());
                    }
                }
            }
        }

        return new PageResponse<>(resultQueue, index, 10);
    }
}
