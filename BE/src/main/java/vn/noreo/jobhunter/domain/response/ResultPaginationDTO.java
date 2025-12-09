package vn.noreo.jobhunter.domain.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResultPaginationDTO {

    private Meta meta;

    // Dữ liệu trả về từ API, có thể là danh sách người dùng hoặc một đối tượng
    // người dùng cụ thể
    private Object result;

    @Getter
    @Setter
    public static class Meta {
        // Trang người dùng truyền vào
        private int page;

        // Số lượng bản ghi trên một trang
        private int pageSize;

        // Tổng số trang trong cơ sở dữ liệu
        private int pages;

        // Tổng số bản ghi trong cơ sở dữ liệu
        private long total;
    }
}
