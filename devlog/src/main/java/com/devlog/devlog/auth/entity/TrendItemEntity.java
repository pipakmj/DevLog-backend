package com.devlog.devlog.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "trend_item", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "type", "originId" })
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrendItemEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private String originId;

    private String title;

    private String url;

    private String thumbnailUrl;

    private int viewCount;

    private int bookmarkCount;

    @Column(columnDefinition = "json")
    @JdbcTypeCode(SqlTypes.JSON)
    private String metadata;

    @OneToMany(mappedBy = "trenditem", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<BookmarksEntity> bookmarks = new ArrayList<>();
}
