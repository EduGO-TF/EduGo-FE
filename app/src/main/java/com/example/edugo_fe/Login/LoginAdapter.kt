package com.example.edugo_fe.Login

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class LoginAdapter(loginActivity: LoginActivity) : FragmentStateAdapter(loginActivity){
    private lateinit var loginAdapter : LoginAdapter
    val fragments = listOf<Fragment>(StartLandingFragment(), SecondLandingFragment(), LoginMainFragment())
    override fun getItemCount(): Int {
        return fragments.size
    }

    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }

}