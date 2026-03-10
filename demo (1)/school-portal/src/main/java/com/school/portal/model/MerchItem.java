package com.school.portal.model;

import jakarta.persistence.*;
import java.util.Set;

@Entity
@Table(name = "MerchItem")
public class MerchItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MerchId")
    private Integer merchId;

    @Column(name = "Name", nullable = false, length = 100)
    private String name;

    @Column(name = "Text", columnDefinition = "TEXT")
    private String text;

    @Column(name = "ImageUrl", length = 500)
    private String imageUrl;

    @Column(name = "Price", nullable = false)
    private Integer price;

    @OneToMany(mappedBy = "merchItem")
    private Set<MerchRequest> merchRequests;

    public Integer getMerchId() { return merchId; }
    public void setMerchId(Integer merchId) { this.merchId = merchId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public Integer getPrice() { return price; }
    public void setPrice(Integer price) { this.price = price; }

    public Set<MerchRequest> getMerchRequests() { return merchRequests; }
    public void setMerchRequests(Set<MerchRequest> merchRequests) { this.merchRequests = merchRequests; }
}