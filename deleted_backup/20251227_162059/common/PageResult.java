package com.blockchain.certificate.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 分页响应结果
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 当前页数据
     */
    private List<T> items;

    /**
     * 当前页码
     */
    private Long page;

    /**
     * 每页大小
     */
    private Long size;

    /**
     * 总页数
     */
    private Long pages;

    public PageResult(Long total, List<T> items) {
        this.total = total;
        this.items = items;
    }
    
    /**
     * 自定义Builder，支持records别名
     */
    public static <T> PageResultBuilder<T> builder() {
        return new PageResultBuilder<T>();
    }
    
    public static class PageResultBuilder<T> {
        private Long total;
        private List<T> items;
        private Long page;
        private Long size;
        private Long pages;
        
        PageResultBuilder() {
        }
        
        public PageResultBuilder<T> total(Long total) {
            this.total = total;
            return this;
        }
        
        public PageResultBuilder<T> items(List<T> items) {
            this.items = items;
            return this;
        }
        
        public PageResultBuilder<T> records(List<T> records) {
            this.items = records;
            return this;
        }
        
        public PageResultBuilder<T> page(Long page) {
            this.page = page;
            return this;
        }
        
        public PageResultBuilder<T> current(Long current) {
            this.page = current;
            return this;
        }
        
        public PageResultBuilder<T> size(Long size) {
            this.size = size;
            return this;
        }
        
        public PageResultBuilder<T> pages(Long pages) {
            this.pages = pages;
            return this;
        }
        
        public PageResult<T> build() {
            PageResult<T> result = new PageResult<T>();
            result.total = this.total;
            result.items = this.items;
            result.page = this.page;
            result.size = this.size;
            result.pages = this.pages;
            return result;
        }
    }
}
