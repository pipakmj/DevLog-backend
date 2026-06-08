package com.devlog.devlog.auth.controller;

import com.devlog.devlog.auth.dto.bookmark.BookmarkRequestDto;
import com.devlog.devlog.auth.dto.bookmark.BookmarkResponseDto;
import com.devlog.devlog.auth.service.BookmarkService;
import com.devlog.devlog.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookmarks")
@RequiredArgsConstructor
public class BookmarksController {

    private final BookmarkService bookmarkService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<BookmarkResponseDto>>> getBookmarks(Authentication authentication) {
        List<BookmarkResponseDto> bookmarks = bookmarkService.getBookmarks(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("북마크 리스트를 성공적으로 가져왔습니다.", bookmarks));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BookmarkResponseDto>> addBookmark(
            Authentication authentication,
            @RequestBody BookmarkRequestDto request) {
        BookmarkResponseDto bookmark = bookmarkService.addBookmark(authentication.getName(), request);
        return ResponseEntity.ok(ApiResponse.success("북마크가 성공적으로 추가되었습니다.", bookmark));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBookmark(
            Authentication authentication,
            @PathVariable Long id) {
        bookmarkService.deleteBookmark(authentication.getName(), id);
        return ResponseEntity.ok(ApiResponse.success("북마크가 성공적으로 삭제되었습니다."));
    }
}
