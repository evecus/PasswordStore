package com.passmanager.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.passmanager.data.model.Group
import com.passmanager.data.model.PasswordEntry
import com.passmanager.data.repository.PasswordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val passwords: List<PasswordEntry> = emptyList(),
    val groups: List<Group> = emptyList(),
    val searchQuery: String = "",
    val selectedGroupId: Long? = null, // null = All, -1 = Favorites
    val isLoading: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: PasswordRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _selectedGroupId = MutableStateFlow<Long?>(null)

    val uiState: StateFlow<HomeUiState> = combine(
        _searchQuery,
        _selectedGroupId,
        repository.getAllGroups()
    ) { query, groupId, groups ->
        Triple(query, groupId, groups)
    }.flatMapLatest { (query, groupId, groups) ->
        val passwordFlow = when {
            query.isNotEmpty() -> repository.searchPasswords(query)
            groupId == -1L -> repository.getFavoritePasswords()
            groupId != null -> repository.getPasswordsByGroup(groupId)
            else -> repository.getAllPasswords()
        }
        passwordFlow.map { passwords ->
            HomeUiState(
                passwords = passwords,
                groups = groups,
                searchQuery = query,
                selectedGroupId = groupId
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState(isLoading = true))

    fun setSearchQuery(query: String) { _searchQuery.value = query }
    fun selectGroup(groupId: Long?) { _selectedGroupId.value = groupId }

    fun toggleFavorite(entry: PasswordEntry) = viewModelScope.launch {
        repository.toggleFavorite(entry)
    }

    fun deletePassword(entry: PasswordEntry) = viewModelScope.launch {
        repository.deletePassword(entry)
    }

    fun getPasswordCountForGroup(groupId: Long): Flow<Int> =
        repository.getPasswordCountByGroup(groupId)

    fun decryptPassword(encrypted: String) = repository.decryptPassword(encrypted)
}

@HiltViewModel
class PasswordEditViewModel @Inject constructor(
    private val repository: PasswordRepository
) : ViewModel() {

    private val _entry = MutableStateFlow<PasswordEntry?>(null)
    val entry: StateFlow<PasswordEntry?> = _entry.asStateFlow()

    val groups: StateFlow<List<Group>> = repository.getAllGroups()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun loadPassword(id: Long) = viewModelScope.launch {
        _entry.value = repository.getPasswordById(id)
    }

    fun decryptPassword(encrypted: String) = repository.decryptPassword(encrypted)

    fun savePassword(entry: PasswordEntry, onSuccess: () -> Unit) = viewModelScope.launch {
        repository.savePassword(entry)
        onSuccess()
    }

    fun deletePassword(entry: PasswordEntry, onSuccess: () -> Unit) = viewModelScope.launch {
        repository.deletePassword(entry)
        onSuccess()
    }
}

@HiltViewModel
class GroupViewModel @Inject constructor(
    private val repository: PasswordRepository
) : ViewModel() {

    val groups: StateFlow<List<Group>> = repository.getAllGroups()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentGroup = MutableStateFlow<Group?>(null)
    val currentGroup: StateFlow<Group?> = _currentGroup.asStateFlow()

    fun loadGroup(id: Long) = viewModelScope.launch {
        _currentGroup.value = repository.getGroupById(id)
    }

    fun saveGroup(group: Group, onSuccess: () -> Unit) = viewModelScope.launch {
        repository.saveGroup(group)
        onSuccess()
    }

    fun deleteGroup(group: Group, onSuccess: () -> Unit) = viewModelScope.launch {
        repository.deleteGroup(group)
        onSuccess()
    }

    fun getPasswordCountForGroup(groupId: Long): Flow<Int> =
        repository.getPasswordCountByGroup(groupId)
}
