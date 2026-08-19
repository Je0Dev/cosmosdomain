package com.papel.imdb_clone.service.content;

import com.papel.imdb_clone.model.content.Content;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Base abstract class for content services providing common CRUD operations.
 * CRUD operations are Create, Read, Update, and Delete.
 * @param <T> The type of content this service manages, must extend Content
 */
public abstract class BaseContentService<T extends Content> implements ContentService<T> {

    //list of content
    protected final List<T> contentList = new ArrayList<>(); //content list
    protected final AtomicInteger nextId = new AtomicInteger(1);
    protected final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    protected final Class<T> contentType;


    //loads content from file
    protected abstract void loadFromFile();


    /**
     * Initialize sample data for the service.
     * This method should be implemented by concrete classes to provide their own sample data.
     */
    protected abstract void initializeSampleData();


    //constructor for base content service
    protected BaseContentService(Class<T> contentType) {
        this.contentType = contentType;
    }


    /**
     * Initialize the service after construction.
     * This should be called by subclasses after they are fully constructed.
     * This method is final and cannot be overridden by subclasses.
     */
    protected final void initializeService() {
        initializeSampleData();
    }


    //return all content
    @Override
    public List<T> getAll() {
        //read lock which means that other threads can read the list but cannot modify it
        lock.readLock().lock();
        try {
            //return copy of content list
            return new ArrayList<>(contentList);
        } finally {
            lock.readLock().unlock();
        }
    }

    //return content by id
    @Override
    public Optional<T> getById(int id) {
        //read lock which means that other threads can read the list but cannot modify it
        lock.readLock().lock();
        try {
            //return first content that matches the id
            return contentList.stream()
                    .filter(content -> content.getId() == id)
                    .findFirst();
        } finally {
            lock.readLock().unlock();
        }
    }

    //save content
    @Override
    public T save(T content) {
        lock.writeLock().lock();
        try {
            if (content.getId() == 0) {  // New content
                content.setId(nextId.getAndIncrement());
                contentList.add(content);
            } else {  // Existing content
                // Find and update existing content
                for (int i = 0; i < contentList.size(); i++) {
                    if (contentList.get(i).getId() == content.getId()) {
                        contentList.set(i, content);
                        break;
                    }
                }
            }
            return content;
        } finally {
            lock.writeLock().unlock();
        }
    }

    //update content
    @Override
    public T update(T content) {
        lock.writeLock().lock();
        try {
            int index = -1;
            //find index of content with given id
            for (int i = 0; i < contentList.size(); i++) {
                if (contentList.get(i).getId() == content.getId()) {
                    index = i;
                    break;
                }
            }
            
            if (index != -1) {
                contentList.set(index, content);
                return content;
            }
            throw new NoSuchElementException("Content with id " + content.getId() + " not found");
        } finally {
            lock.writeLock().unlock();
        }
    }

    //delete content by id
    @Override
    public boolean delete(int id) {
        lock.writeLock().lock();
        try {
            //remove content with given id
            return contentList.removeIf(content -> content.getId() == id);
        } finally {
            lock.writeLock().unlock();
        }
    }

    //remove content
    @Override
    public boolean remove(T content) {
        if (content == null) {
            return false;
        }
        return delete(content.getId());
    }

    //find content by title and year
    @Override
    public Optional<T> findByTitleAndYear(String title, int year) {
        //read lock which means that other threads can read the list but cannot modify it
        lock.readLock().lock();
        try {
            // Convert java.util.Date to java.time.Instant then to LocalDate to get the year
            return contentList.stream()
                    .filter(content -> content.getTitle().equalsIgnoreCase(title) && 
                                     content.getReleaseDate().toInstant()
                                            .atZone(java.time.ZoneId.systemDefault())
                                            .getYear() == year)
                    .findFirst();
        } finally {
            //unlock the read lock when done, which means that other threads can read the list
            lock.readLock().unlock();
        }
    }

    @Override
    public String getContentType() {
        return contentType.getSimpleName();
    }


}
