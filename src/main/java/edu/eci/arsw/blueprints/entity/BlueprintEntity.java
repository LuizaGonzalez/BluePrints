package edu.eci.arsw.blueprints.entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "blueprints",
        uniqueConstraints = @UniqueConstraint(columnNames = {"author", "name"})
)
public class BlueprintEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private String author;

    @Column(nullable = false)
    private String name;

    //orphanRemoval = true -> si eliminas un punto de la lista, se elimina de la BD
    //mappedBy = "blueprint" -> el lado dueño de la relación es PointEntity (tiene un campo llamado "blueprint")
    @OneToMany(mappedBy = "blueprint", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<PointEntity> points = new ArrayList<>();

    protected BlueprintEntity() { }

    public BlueprintEntity(String author, String name) {
        this.author = author;
        this.name = name;
    }
    public Long getId() { return id; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<PointEntity> getPoints() { return points; }

    public void addPoint(PointEntity point) {
        points.add(point);
        point.setBlueprint(this);
    }

}
