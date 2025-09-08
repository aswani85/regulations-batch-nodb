package com.example.regulationsbatch.model;

import lombok.Data;
import java.util.List;

@Data
public class CommentRecord {
    private String commentId;
    private String agencyId;
    private String documentType;
    private String lastModifiedDate;
    private String objectId;
    private String postedDate;
    private String title;
    private Integer withdrawn;

    // detail
    private String category;
    private String city;
    private String comment;
    private String commentOn;
    private String commentOnDocumentId;
    private String country;
    private String docAbstract;
    private String docketId;
    private Integer duplicateComments;
    private String field1;
    private String field2;
    private String firstName;
    private String govAgency;
    private String govAgencyType;
    private String lastName;
    private String legacyId;
    private String modifyDate;
    private Integer openForComment;
    private String organization;
    private String originalDocumentId;
    private String pageCount;
    private String postmarkDate;
    private String reasonWithdrawn;
    private String receiveDate;
    private String restrictReason;
    private String restrictReasonType;
    private String stateProvinceRegion;
    private String submitterRep;
    private String submitterRepCityState;
    private String subtype;
    private String trackingNbr;
    private String zip;
    private String attachmentLinks;

    private List<String> attachmentUrls;
}
