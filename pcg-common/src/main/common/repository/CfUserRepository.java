package common.repository;

import common.base.BaseRepository;
import common.entity.CfUser;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CfUserRepository extends BaseRepository<CfUser> {

	Optional<CfUser> findByUsername(String username);

	@Query("""
		    SELECT u FROM CfUser u
		    JOIN FETCH u.organizationalStructure os
		    JOIN FETCH u.office o
		    WHERE u.username = :username
		""")
	Optional<CfUser> findByUsernameForSecurity(@Param("username") String username);

	@Query(value = """
    SELECT TOP 1
        COALESCE(
            NULLIF(LTRIM(RTRIM(CONCAT(
                COALESCE(u.FirstName, ''),
                CASE
                    WHEN u.FirstName IS NOT NULL AND u.LastName IS NOT NULL THEN ' '
                    ELSE ''
                END,
                COALESCE(u.LastName, '')
            ))), ''),
            CONVERT(nvarchar(255), u.Username),
            CONVERT(nvarchar(50), u.IdUser)
        )
			FROM dbo.CF_User u
			INNER JOIN dbo.CF_Role r
        ON r.IdRole = u.IdRole_Fk
			WHERE r.Code = 'DIR'
      AND u.IdOrganizationalStructure_Fk = :idStructure
      AND ISNULL(u.IsActive, 1) = 1
    ORDER BY u.IdUser ASC
    """, nativeQuery = true)
	Optional<String> findDirNameByStructureId(@Param("idStructure") Long idStructure);

	@Query("SELECT u FROM CfUser u JOIN FETCH u.role r " +
			"WHERE u.idOfficeFk = :idOffice " +
			"AND r.code = :roleCode " +
			"AND (u.isMarked IS NULL OR u.isMarked = false)")
	Optional<CfUser> findDirettoreUfficio(@Param("idOffice") Long idOffice, @Param("roleCode") String roleCode);



	@Query("""
    SELECT u FROM CfUser u
    JOIN u.role r
    WHERE UPPER(TRIM(r.code)) = UPPER(TRIM(:roleCode))
      AND u.idOrganizationalStructureFk = :structureId
      AND (u.isActive IS NULL OR u.isActive = true)
      AND (u.isMarked IS NULL OR u.isMarked = false)
""")
	List<CfUser> findActiveUsersByRoleCodeAndStructure(
			@Param("roleCode") String roleCode,
			@Param("structureId") Long structureId
	);

	@Query("""
    SELECT u FROM CfUser u
    WHERE u.idOrganizationalStructureFk = :structureId
      AND (u.isActive IS NULL OR u.isActive = true)
      AND (u.isMarked IS NULL OR u.isMarked = false)
""")
	List<CfUser> findActiveUsersByStructure(@Param("structureId") Long structureId);

	@Query("""
    SELECT u FROM CfUser u
    WHERE (u.isActive IS NULL OR u.isActive = true)
      AND (u.isMarked IS NULL OR u.isMarked = false)
""")
	List<CfUser> findAllActiveUsers();

}
