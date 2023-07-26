package com.concepts.conceptsDemo;

import com.concepts.conceptsDemo.domain.CalculatingConcept;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
public class ConceptRepository {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public Map<String, CalculatingConcept> getCalculatingConcept() {
        String sql = "SELECT c.con_nombre AS nombre, c.con_tipo_valor AS tipo_valor,\n" +
                "       c.con_asocia_entidad AS asocia_entidad, c.con_reporte AS reporte,\n" +
                "       CASE\n" +
                "           WHEN c.con_tipo_valor = 'FORMULACION' THEN cf.con_for_formulacion\n" +
                "           WHEN c.con_tipo_valor = 'NUMERICO' THEN cn.con_num_valor\n" +
                "           ELSE NULL\n" +
                "       END AS valor,\n" +
                "       CASE\n" +
                "           WHEN c.con_tipo_valor = 'FORMULACION' THEN cf.con_prioridad\n" +
                "           ELSE NULL\n" +
                "       END AS prioridad\n" +
                "FROM CONCEPTO c\n" +
                "LEFT JOIN CONCEPTO_FORMULACION cf ON c.con_nombre = cf.fk_con_nombre\n" +
                "LEFT JOIN CONCEPTO_NUMERICO cn ON c.con_nombre = cn.fk_con_nombre";
        Map<String, CalculatingConcept> concepts = new HashMap<>();
        jdbcTemplate.query(sql, rs -> {
            CalculatingConcept concept = new CalculatingConcept();
            concept.setValueType(rs.getString("tipo_valor"));
            concept.setValue(rs.getString("valor"));
            concept.setAssociatesEntity(rs.getInt("asocia_entidad") == 1);
            concept.setHasReport(rs.getInt("reporte") == 1);
            concept.setPriority(rs.getInt("prioridad"));
            concepts.put(rs.getString("nombre"), concept);
        });

        return concepts;
    }
}
