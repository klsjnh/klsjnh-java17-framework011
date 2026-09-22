package com.klsjnh.infrastructure.persistence.repository;

/*                BaseMasterSubRepository011Test class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.22
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.22  unit test for the saveMaster insert/update branches (031/018)
 *
 */

import com.klsjnh.infrastructure.persistence.entity.BasePo;
import com.klsjnh.infrastructure.persistence.mapper.CommonMapper;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
 * Unit tests for the master-sub base three-grain API: {@code saveMaster} must
 * insert on a blank id, update on a present id, and reject a null master.
 */

class BaseMasterSubRepository011Test {

    /**
     * Minimal concrete repository over the generic {@link BasePo}.
     */
    private static final class TestRepo extends BaseMasterSubRepository011<BasePo, BaseMapper<BasePo>> {

        /**
         * Create the repository.
         *
         * @param mapper       mybatis-plus mapper
         * @param commonMapper native sql mapper
         */
        private TestRepo(BaseMapper<BasePo> mapper, CommonMapper commonMapper) {
            super(mapper, commonMapper);
        }

        /** {@inheritDoc} */
        @Override
        protected String getTableName() {
            return "t_test";
        }

        /** {@inheritDoc} */
        @Override
        protected String getBusinessColumn() {
            return null;
        }

        /** {@inheritDoc} */
        @Override
        protected List<BaseRepository<?, ?>> getChildServices() {
            return List.of();
        }
    }

    /**
     * A blank id inserts the master and never updates.
     */
    @Test
    void saveMasterInsertsWhenIdBlank() {
        BaseMapper<BasePo> mapper = Mockito.mock(BaseMapper.class);
        TestRepo repo = new TestRepo(mapper, Mockito.mock(CommonMapper.class));
        BasePo master = new BasePo();

        repo.saveMaster(master);

        Mockito.verify(mapper, Mockito.times(1)).insert(master);
        Mockito.verify(mapper, Mockito.never()).updateById(Mockito.any(BasePo.class));
    }

    /**
     * A present id updates the master and never inserts.
     */
    @Test
    void saveMasterUpdatesWhenIdPresent() {
        BaseMapper<BasePo> mapper = Mockito.mock(BaseMapper.class);
        TestRepo repo = new TestRepo(mapper, Mockito.mock(CommonMapper.class));
        BasePo master = new BasePo();

        master.setId("m1");
        Mockito.when(mapper.updateById(master)).thenReturn(1);

        repo.saveMaster(master);

        Mockito.verify(mapper, Mockito.times(1)).updateById(master);
        Mockito.verify(mapper, Mockito.never()).insert(Mockito.any(BasePo.class));
    }

    /**
     * A null master is rejected.
     */
    @Test
    void saveMasterRejectsNull() {
        BaseMapper<BasePo> mapper = Mockito.mock(BaseMapper.class);
        TestRepo repo = new TestRepo(mapper, Mockito.mock(CommonMapper.class));

        try {
            repo.saveMaster(null);
            org.junit.jupiter.api.Assertions.fail("expected bad request for null master");
        } catch (RuntimeException ex) {
            org.junit.jupiter.api.Assertions.assertNotNull(ex.getMessage());
        }
    }
}
