package edu.eci.arsw.blueprints.persistence;

import edu.eci.arsw.blueprints.entity.BlueprintEntity;
import edu.eci.arsw.blueprints.entity.BlueprintJpaRepository;
import edu.eci.arsw.blueprints.entity.PointEntity;
import edu.eci.arsw.blueprints.model.Blueprint;
import edu.eci.arsw.blueprints.model.Point;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
@Profile("postgres")
public class PostgresBlueprintPersistence implements BlueprintPersistence {

    private final BlueprintJpaRepository repository;

    public PostgresBlueprintPersistence(BlueprintJpaRepository blueprintJpaRepository)
    {
        this.repository = blueprintJpaRepository;
    }

    @Override
    @Transactional
    public void saveBlueprint(Blueprint blueprint) throws BlueprintPersistenceException {
        if(repository.existsByAuthorAndName(blueprint.getAuthor(),blueprint.getName())){
            throw new BlueprintPersistenceException("Blueprint already exits: " + blueprint.getAuthor()+":"+blueprint.getName());
        }
        repository.save(toEntity(blueprint));
    }
    @Override
    @Transactional(readOnly = true)
    public Blueprint getBlueprint(String author, String name) throws BlueprintNotFoundException {
        BlueprintEntity entity = repository.findByAuthorAndName(author,name)
                .orElseThrow(()->new BlueprintNotFoundException("Blueprint not found %s/%s".formatted(author, name)));
        return toDomain(entity);
    }
    @Override
    @Transactional(readOnly = true)
    public Set<Blueprint> getBlueprintsByAuthor(String author) throws BlueprintNotFoundException {
        Set<BlueprintEntity> entities = repository.findByAuthor(author);
        if(entities.isEmpty()){
            throw new BlueprintNotFoundException("No blueprints for author: "+ author);
        }
        return entities.stream().map(this::toDomain).collect(Collectors.toSet());
    }
    @Override
    @Transactional
    public Set<Blueprint> getAllBlueprints() {
        return repository.findAll().stream().map(this::toDomain).collect(Collectors.toSet());
    }

    @Override
    @Transactional
    public void addPoint(String author, String name, int x, int y) throws BlueprintNotFoundException {
        BlueprintEntity entity = repository.findByAuthorAndName(author, name)
                .orElseThrow(() -> new BlueprintNotFoundException(
                        "Blueprint not found: %s/%s".formatted(author, name)));
        entity.addPoint(new PointEntity(x, y));
        repository.save(entity);
    }

    private BlueprintEntity toEntity(Blueprint bp) {
        BlueprintEntity entity = new BlueprintEntity(bp.getAuthor(), bp.getName());
        for (Point p : bp.getPoints()) {
            entity.addPoint(new PointEntity(p.x(), p.y()));
        }
        return entity;
    }
    private Blueprint toDomain(BlueprintEntity entity) {
        List<Point> points = entity.getPoints().stream()
                .map(pe -> new Point(pe.getX(), pe.getY()))
                .toList();
        return new Blueprint(entity.getAuthor(), entity.getName(), points);
    }

}
