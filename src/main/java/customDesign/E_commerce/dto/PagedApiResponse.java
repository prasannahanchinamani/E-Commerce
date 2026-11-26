package customDesign.E_commerce.dto;

import java.util.List;

public class PagedApiResponse<T> {
    private boolean success;
    private List<T> data;
    private PageInfo page;

    public PagedApiResponse(boolean success, List<T> data, PageInfo page) {
        this.success = success;
        this.data = data;
        this.page = page;
    }

    public boolean isSuccess() { return success; }
    public List<T> getData() { return data; }
    public PageInfo getPage() { return page; }
}