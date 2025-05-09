package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.exception.SetmealEnableFailedException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetMealDishMapper;
import com.sky.mapper.SetMealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class SetmealServiceImpl implements SetmealService {

    @Autowired
    private SetMealMapper setMealMapper;

    @Autowired
    private SetMealDishMapper setMealDishMapper;

    @Autowired
    private DishMapper dishMapper;

    /**
     * 新增套餐
     * @param setmealDTO
     */
    @Transactional
    public void saveWithDish(SetmealDTO setmealDTO) {

        //往套餐表中新增套餐信息
        Setmeal setMeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setMeal);

        setMealMapper.insert(setMeal);

//        1.往套餐菜品表中新增套餐菜品信
//        2.批量插入
        //和菜品管理中同时新增菜品和口味不同，由于新增菜品时dishId尚未生成，因此需要获取dishId赋值给flavor对象
        //而新增套餐时就已经存在菜品了，因此前端可以将dishId一起发送过来

        Long SetmealId = setMeal.getId();
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        if(setmealDishes != null && !setmealDishes.isEmpty()){
            setmealDishes.forEach(setmealDish -> setmealDish.setSetmealId(SetmealId));
        }
        setMealDishMapper.insertBatch(setmealDishes);




    }

    /**
     * 分页查询
     * @param setmealPageQueryDTO
     * @return
     */
    public PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {
        PageHelper.startPage(setmealPageQueryDTO.getPage(), setmealPageQueryDTO.getPageSize());
        Page<SetmealVO> page = setMealMapper.pageQuery(setmealPageQueryDTO);

        Long total = page.getTotal();
        List<SetmealVO> records = page.getResult();
        return new PageResult(total, records);
    }

    /**
     * 根据id查询套餐以及对应菜品
     * @param id
     * @return
     */
    public SetmealVO getSetmealWithDish(Long id) {
        Setmeal setmeal = setMealMapper.getById(id);
        List<SetmealDish> setmealDishes = setMealDishMapper.getBySetmealId(id);

        SetmealVO setmealVO = new SetmealVO();
        BeanUtils.copyProperties(setmeal, setmealVO);
        setmealVO.setSetmealDishes(setmealDishes);
        return setmealVO;
    }

    /**
     * 批量删除套餐
     * @param ids
     */
    public void deleteBatch(List<Long> ids) {

//        for (Long id : ids) {
//            Setmeal setmeal = setMealMapper.getById(id);
//            //如果套餐是启售的，则不能删除
//            if(setmeal.getStatus().equals(StatusConstant.DISABLE)){
//                setMealMapper.deleteById(id);
//            }else{
//                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
//            }
//        }
        for (Long id : ids) {
            Setmeal setmeal = setMealMapper.getById(id);
            //如果套餐是启售的，则不能删除
            if (setmeal.getStatus().equals(StatusConstant.ENABLE)) {
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
            }
        }

        //同时删除套餐和套餐菜品对应关系
        setMealMapper.deleteByIds(ids);
        setMealDishMapper.deleteByIds(ids);
    }

    /**
     * 修改套餐
     * @param setmealDTO
     */
    @Transactional
    public void updateWithSetMealDish(SetmealDTO setmealDTO) {
        //更新套餐
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        setMealMapper.update(setmeal);

        //更新套餐菜品关系--删除原有的关系，重新插入新的关系
        List<Long> setmealIds = new ArrayList<>();
        setmealIds.add(setmealDTO.getId());
        setMealDishMapper.deleteByIds(setmealIds);
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        setmealDishes.forEach(setmealDish -> setmealDish.setSetmealId(setmealDTO.getId()));
        setMealDishMapper.insertBatch(setmealDishes);
    }

    /**
     * 启用或禁用套餐
     * @param status
     * @param id
     */
    public void startOrStop(Integer status, Long id) {

        if(status.equals(StatusConstant.ENABLE)){
            List<Dish> dishes = dishMapper.getBySetmealId(id);
            for (Dish dish : dishes) {
                if(dish.getStatus().equals(StatusConstant.DISABLE))
                    throw new SetmealEnableFailedException(MessageConstant.SETMEAL_ENABLE_FAILED);
            }
        }
        Setmeal setmeal = Setmeal.builder()
                .id(id)
                .status(status)
                .build();
        setMealMapper.update(setmeal);
    }
}
