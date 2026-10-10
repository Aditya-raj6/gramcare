#!/usr/bin/env bash
# Developer helper: scaffolds Repository/Service/Controller for an entity.
# usage: gen-crud.sh <module> <package> <Entity> <api-path> <Human Name>
set -e
MOD=$1; PKG=$2; ENT=$3; PATHX=$4; HUMAN=$5
B=$MOD/src/main/java/com/healthharbor/$PKG
mkdir -p $B/repository $B/service $B/controller
[ -f $B/repository/${ENT}Repository.java ] || cat > $B/repository/${ENT}Repository.java <<J
package com.healthharbor.$PKG.repository;

import com.healthharbor.common.crud.CrudRepository;
import com.healthharbor.$PKG.entity.$ENT;

public interface ${ENT}Repository extends CrudRepository<$ENT> {
}
J
[ -f $B/service/${ENT}Service.java ] || cat > $B/service/${ENT}Service.java <<J
package com.healthharbor.$PKG.service;

import com.healthharbor.common.crud.CrudService;
import com.healthharbor.$PKG.entity.$ENT;
import com.healthharbor.$PKG.repository.${ENT}Repository;
import org.springframework.stereotype.Service;

@Service
public class ${ENT}Service extends CrudService<$ENT> {
    public ${ENT}Service(${ENT}Repository repository) {
        super(repository, $ENT.class, "$HUMAN");
    }
}
J
[ -f $B/controller/${ENT}Controller.java ] || cat > $B/controller/${ENT}Controller.java <<J
package com.healthharbor.$PKG.controller;

import com.healthharbor.common.crud.CrudController;
import com.healthharbor.$PKG.entity.$ENT;
import com.healthharbor.$PKG.service.${ENT}Service;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("$PATHX")
@Tag(name = "$HUMAN")
public class ${ENT}Controller extends CrudController<$ENT> {
    public ${ENT}Controller(${ENT}Service service) {
        super(service);
    }
}
J
echo "scaffolded $ENT"
