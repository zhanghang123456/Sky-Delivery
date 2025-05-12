package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.entity.Setmeal;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetMealDishMapper;
import com.sky.mapper.SetMealMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private DishFlavorMapper dishFlavorMapper;

    @Autowired
    private SetMealDishMapper setMealDishMapper;

    @Autowired
    private SetMealMapper setMealMapper;

    /**
     * 新增菜品以及对应的口味
     * @param dishDTO
     * @Transactional 对于涉及多个表的(插入，更新，删除)要开启事务管理，事务中的所有操作要么全部成功，要么全部失败回滚到事务开始前的状态。不会出现部分成功、部分失败的情况。
     */
    @Transactional
    public void saveWithFlavor(DishDTO dishDTO) {

        //由于DishDTO中包含口味，所以直接将DishDTO中关于菜品的属性单独创建Dish对象
        Dish dish = new Dish();

        //利用BeanUtils将DishDTO中的属性复制到Dish中
        BeanUtils.copyProperties(dishDTO, dish);

        dishMapper.insert(dish);

        //将菜品id赋值给口味表中的菜品id

        Long dishId = dish.getId();

        //新增口味数据

        //获取口味列表
        List<DishFlavor> flavors = dishDTO.getFlavors();
        //判断口味列表是否为空
        if (flavors != null && !flavors.isEmpty()) {
            //遍历口味列表，将菜品id赋值给口味表中的菜品id
            flavors.forEach(dishFlavor -> dishFlavor.setDishId(dishId));
            //批量插入口味
            dishFlavorMapper.insertBatch(flavors);

        }

    }

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());
        Page<DishVO> page = dishMapper.pageQuery(dishPageQueryDTO);

        long total = page.getTotal();
        List<DishVO> records = page.getResult();

        return new PageResult(total, records);
    }

    /**
     * 批量删除菜品
     * @param ids
     */
    @Transactional
    public void deleteBatch(List<Long> ids) {

        //以下为删除菜品的业务逻辑

        //判断是否存在起售中的菜品，若存在则不能删除
        for (Long id : ids) {
            Dish dish = dishMapper.getById(id);
            if(dish.getStatus() == StatusConstant.ENABLE){
                //菜品处于启售状态不能删除,DeletionNotAllowedException为自定义异常类，抛出的异常将由前端处理
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }

        //判断菜品是否与套餐关联，若存在则不能删除
        List<Long> setMealIds = setMealDishMapper.getSetMealIdsByDishIds(ids);
        if(setMealIds != null && !setMealIds.isEmpty()){
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }

        //删除菜品表中数据
//        for (Long id : ids) {
//            dishMapper.deleteById(id);
//            //删除菜品口味表中数据
//            dishFlavorMapper.deleteByDishId(id);
//        }

        //以下为参数菜品功能优化：使用批量删除sql减少发送sql的次数

        //批量删除菜品表中数据
        dishMapper.deleteByIds(ids);


        //批量删除口味表中数据

        dishFlavorMapper.deleteByDishIds(ids);

    }

    /**
     * 根据id查询菜品信息和口味信息
     * @param id
     * @return
     */
    public DishVO getByIdWithFlavor(Long id) {
        //根据id查询菜品信息
        Dish dish = dishMapper.getById(id);

        //根据菜品id查询口味信息,由于一个菜品可以对应多条口味信息因此为List类型
        List<DishFlavor> dishFlavors = dishFlavorMapper.getByDishId(id);

        //将数据封装到VO中
        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish,dishVO);
        dishVO.setFlavors(dishFlavors);
        return dishVO;
    }

    /**
     * 修改菜品以及对应的口味
     * @param dishDTO
     */
    @Transactional
    public void updateWithFlavor(DishDTO dishDTO) {

        //修改菜品信息
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO,dish);

        dishMapper.update(dish);

        //删除原有口味信息
        dishFlavorMapper.deleteByDishId(dishDTO.getId());

        //添加新的口味信息

        //获取口味列表
        List<DishFlavor> flavors = dishDTO.getFlavors();
        //判断口味列表是否为空
        if (flavors != null && !flavors.isEmpty()) {
            //遍历口味列表，将菜品id赋值给口味表中的菜品id
            flavors.forEach(dishFlavor -> dishFlavor.setDishId(dishDTO.getId()));
            //批量插入口味
            dishFlavorMapper.insertBatch(flavors);

        }
    }

    /**
     * 启售禁售菜品
     * @param status
     * @param id
     */
    public void startOrStop(Integer status, Long id) {

        //利用Bulider构建器构建dish对象,因为update方法需要传入dish对象
        Dish dish = Dish.builder()
                .id(id)
                .status(status)
                .build();
        dishMapper.update(dish);

        //如果是停售操作，需要将包含该菜品的套餐也停售
        if(status.equals(StatusConstant.DISABLE)){
            List<Long> dishIds = new ArrayList<>();
            dishIds.add(id);
            List<Long> setMealIds = setMealDishMapper.getSetMealIdsByDishIds(dishIds);
//            List<Setmeal> setMeals = new ArrayList<>(); 逻辑简化：如果只是采用依个更新的方法的话，没必要将setMeal对象添加进集合中
            if(setMealIds != null && !setMealIds.isEmpty()){
                for (Long setMealId : setMealIds) {
                    Setmeal setMeal = Setmeal.builder()
                            .id(setMealId)
                            .status(status)
                            .build();
                    setMealMapper.update(setMeal);
                }
            }



        }


    }

    /**
     * 根据分类id查询菜品
     * @param categoryId
     * @return
     */
    public List<Dish> listByCategoryId(Long categoryId) {
        return dishMapper.listByCategoryId(categoryId);
    }

    /**
     * 条件查询菜品和口味
     * @param dish
     * @return
     */
    public List<DishVO> listWithFlavor(Dish dish) {
        List<Dish> dishList = dishMapper.list(dish);

        List<DishVO> dishVOList = new ArrayList<>();

        for (Dish d : dishList) {
            DishVO dishVO = new DishVO();
            BeanUtils.copyProperties(d,dishVO);

            //根据菜品id查询对应的口味
            List<DishFlavor> flavors = dishFlavorMapper.getByDishId(d.getId());

            dishVO.setFlavors(flavors);
            dishVOList.add(dishVO);
        }

        return dishVOList;
    }


}
