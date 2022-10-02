package io.github.etases.edublock.rs.model.input;

public record StudentUpdate(
        String ethnic,
        String fatherName,
        String fatherJob,
        String motherName,
        String motherJob,
        String guardianName,
        String guardianJob,
        String homeTown
) {
    public boolean validate() {
        return ethnic != null && !ethnic.isBlank()
                && fatherName != null
                && fatherJob != null
                && motherName != null
                && motherJob != null
                && guardianName != null
                && guardianJob != null
                && homeTown != null && !homeTown.isBlank();
    }
}
