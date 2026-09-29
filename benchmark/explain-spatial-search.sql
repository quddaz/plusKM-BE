EXPLAIN ANALYZE
SELECT id
FROM emergency
WHERE active = true
  AND ST_Within(
        coordinate,
        ST_GeomFromText(
          'POLYGON((126.864737 37.476668,126.864737 37.656332,127.091263 37.656332,127.091263 37.476668,126.864737 37.476668))',
          4326,
          'axis-order=long-lat'
        )
      );

EXPLAIN ANALYZE
SELECT id
FROM emergency FORCE INDEX (idx_emergency_coordinate)
WHERE active = true
  AND MBRContains(
        ST_GeomFromText(
          'POLYGON((126.864737 37.476668,126.864737 37.656332,127.091263 37.656332,127.091263 37.476668,126.864737 37.476668))',
          4326,
          'axis-order=long-lat'
        ),
        coordinate
      );

EXPLAIN ANALYZE
SELECT id,
       ST_Distance_Sphere(
         coordinate,
         ST_SRID(POINT(126.9780, 37.5665), 4326)
       ) AS distance_meters
FROM emergency FORCE INDEX (idx_emergency_coordinate)
WHERE active = true
  AND MBRContains(
        ST_GeomFromText(
          'POLYGON((126.864737 37.476668,126.864737 37.656332,127.091263 37.656332,127.091263 37.476668,126.864737 37.476668))',
          4326,
          'axis-order=long-lat'
        ),
        coordinate
      )
  AND ST_Distance_Sphere(
        coordinate,
        ST_SRID(POINT(126.9780, 37.5665), 4326)
      ) <= 10000
ORDER BY distance_meters;
