package com.foo.community.entity;


public class Page {
    private int currentPage = 1;//把当前页当作已知，比如说当前页在第三页

    private int limit = 10; //设置每页显示10条数据，则默认limit为10。

    private String path; //查询路径

    //数据总数
    private int rows;

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        if (currentPage >= 1) {
            this.currentPage = currentPage;
        }
    }

    public int getLimit() {
        return limit;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        if (rows > 0) {
            this.rows = rows;
        }

    }

    public void setLimit(int limit) {
        if (limit >= 1 && limit <= 100) {
            this.limit = limit;
        }
    }

    //总页数
    public int getTotal() {
        if (rows % limit == 0) {
            return rows / limit;
        } else {
            return rows / limit + 1;
        }
    }

    //获取当前页的起始位置行号，分页查询的时候需要的参数
    public int getOffset() {
        return (currentPage - 1) * limit;
    }

    //页码一共显示五个，比如说1，2，3，4，5；9，10，11，12，13，14
    //显示当前页码以及前两个页码
    public int getFrom() {
        int from = currentPage - 2;
        return from < 1 ? 1 : from;
    }

    //显示当前页码后两个页码
    public int getTo() {
        int to = currentPage + 2;
        int total = getTotal();
        return to > total ? total : to;
    }

}
