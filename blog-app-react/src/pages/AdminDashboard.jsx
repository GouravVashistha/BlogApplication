import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { getCategories, createCategory, updateCategory, deleteCategory } from '../services/categoryService';
import { getAllPosts, deletePost } from '../services/postService';
import * as userService from '../services/userService';

const AdminDashboard = () => {
  const [activeTab, setActiveTab] = useState('categories'); // 'categories', 'posts', or 'users'
  const [categories, setCategories] = useState([]);
  const [posts, setPosts] = useState([]);
  const [users, setUsers] = useState([]);
  
  // Loading & Error states
  const [loadingCats, setLoadingCats] = useState(false);
  const [loadingPosts, setLoadingPosts] = useState(false);
  const [loadingUsers, setLoadingUsers] = useState(false);
  const [actionLoading, setActionLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  // Category Form State
  const [catFormData, setCatFormData] = useState({ categoryTitle: '', categoryDecription: '' });
  const [editingCatId, setEditingCatId] = useState(null);

  // Pagination for posts
  const [postPageInfo, setPostPageInfo] = useState({
    pageNumber: 0,
    pageSize: 10,
    totalElements: 0,
    totalPages: 0,
    lastPage: true
  });

  useEffect(() => {
    loadCategories();
    loadPosts(0);
    loadUsers();
  }, []);

  const loadCategories = async () => {
    setLoadingCats(true);
    try {
      const data = await getCategories();
      setCategories(data);
    } catch (err) {
      console.error(err);
      setError('Failed to load categories.');
    } finally {
      setLoadingCats(false);
    }
  };

  const loadPosts = async (page = 0) => {
    setLoadingPosts(true);
    try {
      const data = await getAllPosts(page, postPageInfo.pageSize, 'postId', 'desc');
      setPosts(data.content || []);
      setPostPageInfo({
        pageNumber: data.pageNumber,
        pageSize: data.pageSize,
        totalElements: data.totalElements,
        totalPages: data.totalPages,
        lastPage: data.lastpage
      });
    } catch (err) {
      console.error(err);
      setError('Failed to load system posts.');
    } finally {
      setLoadingPosts(false);
    }
  };

  const loadUsers = async () => {
    setLoadingUsers(true);
    try {
      const data = await userService.getAllUsers();
      setUsers(data || []);
    } catch (err) {
      console.error(err);
      setError('Failed to load registered users.');
    } finally {
      setLoadingUsers(false);
    }
  };

  const handlePromoteUser = async (userId, name) => {
    if (window.confirm(`Are you sure you want to promote ${name} to Administrator?`)) {
      setError('');
      setSuccess('');
      setActionLoading(true);
      try {
        await userService.promoteUserToAdmin(userId);
        setSuccess(`${name} promoted to Admin successfully!`);
        await loadUsers();
      } catch (err) {
        console.error(err);
        setError(err.response?.data?.message || `Failed to promote ${name} to Admin.`);
      } finally {
        setActionLoading(false);
      }
    }
  };

  const handleDeleteUser = async (userId, name) => {
    if (window.confirm(`Are you sure you want to delete user ${name}? This action is permanent.`)) {
      setError('');
      setSuccess('');
      setActionLoading(true);
      try {
        await userService.deleteUser(userId);
        setUsers((prev) => prev.filter((u) => u.id !== userId));
        setSuccess(`User ${name} deleted successfully!`);
      } catch (err) {
        console.error(err);
        setError(err.response?.data?.message || `Failed to delete user ${name}.`);
      } finally {
        setActionLoading(false);
      }
    }
  };

  // Category CRUD Handlers
  const handleCatInputChange = (e) => {
    const { name, value } = e.target;
    setCatFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleCatFormSubmit = async (e) => {
    e.preventDefault();
    const { categoryTitle, categoryDecription } = catFormData;

    if (!categoryTitle.trim() || !categoryDecription.trim()) {
      setError('Please fill in all category fields.');
      return;
    }

    if (categoryTitle.trim().length < 4) {
      setError('Category Title must be at least 4 characters.');
      return;
    }

    if (categoryDecription.trim().length < 10) {
      setError('Category Description must be at least 10 characters.');
      return;
    }

    setError('');
    setSuccess('');
    setActionLoading(true);

    try {
      const payload = {
        categoryTitle: categoryTitle.trim(),
        categoryDecription: categoryDecription.trim()
      };

      if (editingCatId) {
        // Update Category
        const updatedCat = await updateCategory(payload, editingCatId);
        setCategories((prev) =>
          prev.map((c) => (c.categoryId === editingCatId ? updatedCat : c))
        );
        setSuccess('Category updated successfully!');
      } else {
        // Create Category
        const newCat = await createCategory(payload);
        setCategories((prev) => [...prev, newCat]);
        setSuccess('Category created successfully!');
      }

      // Reset form
      setCatFormData({ categoryTitle: '', categoryDecription: '' });
      setEditingCatId(null);
    } catch (err) {
      console.error(err);
      setError(err.response?.data?.message || 'Failed to save category.');
    } finally {
      setActionLoading(false);
    }
  };

  const handleStartEditCategory = (cat) => {
    setEditingCatId(cat.categoryId);
    setCatFormData({
      categoryTitle: cat.categoryTitle,
      categoryDecription: cat.categoryDecription
    });
    setError('');
    setSuccess('');
  };

  const handleCancelEditCategory = () => {
    setEditingCatId(null);
    setCatFormData({ categoryTitle: '', categoryDecription: '' });
    setError('');
    setSuccess('');
  };

  const handleDeleteCategory = async (catId) => {
    if (window.confirm('Are you sure you want to delete this category? All associated blogs might be affected.')) {
      setError('');
      setSuccess('');
      setActionLoading(true);
      try {
        await deleteCategory(catId);
        setCategories((prev) => prev.filter((c) => c.categoryId !== catId));
        setSuccess('Category deleted successfully!');
      } catch (err) {
        console.error(err);
        setError(err.response?.data?.message || 'Failed to delete category. Ensure it has no associated posts.');
      } finally {
        setActionLoading(false);
      }
    }
  };

  // Post Deletion Handler
  const handleDeletePost = async (postId) => {
    if (window.confirm('Are you sure you want to delete this post as Admin? This action is permanent.')) {
      setError('');
      setSuccess('');
      setActionLoading(true);
      try {
        await deletePost(postId);
        setPosts((prev) => prev.filter((p) => p.postId !== postId));
        setSuccess('Post deleted successfully!');
      } catch (err) {
        console.error(err);
        setError(err.response?.data?.message || 'Failed to delete post.');
      } finally {
        setActionLoading(false);
      }
    }
  };

  return (
    <div className="admin-dashboard-container">
      {/* Page Header */}
      <section className="profile-header glass-panel" style={{ padding: '30px' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '20px' }}>
          <div className="profile-avatar" style={{ background: 'linear-gradient(135deg, var(--accent) 0%, var(--primary) 100%)', color: '#fff', fontSize: '2rem' }}>
            🛡️
          </div>
          <div>
            <h1 className="gradient-text" style={{ margin: 0, fontSize: '2.2rem' }}>Admin Control Center</h1>
            <p style={{ color: 'var(--text-muted)', marginTop: '6px', fontSize: '1rem' }}>
              Manage categories, moderate system-wide articles, and supervise content.
            </p>
          </div>
        </div>
      </section>

      {/* Stats Cards Grid */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: '20px', marginBottom: '25px', marginTop: '20px' }}>
        <div className="glass-panel" style={{ padding: '20px', display: 'flex', flexDirection: 'column', gap: '8px' }}>
          <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)', fontWeight: 600, textTransform: 'uppercase', letterSpacing: '0.05em' }}>📂 Total Categories</span>
          <span style={{ fontSize: '2.2rem', fontWeight: 800, color: 'var(--primary)' }}>{categories.length}</span>
        </div>
        <div className="glass-panel" style={{ padding: '20px', display: 'flex', flexDirection: 'column', gap: '8px' }}>
          <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)', fontWeight: 600, textTransform: 'uppercase', letterSpacing: '0.05em' }}>📝 Total Articles</span>
          <span style={{ fontSize: '2.2rem', fontWeight: 800, color: '#a5b4fc' }}>{postPageInfo.totalElements}</span>
        </div>
        <div className="glass-panel" style={{ padding: '20px', display: 'flex', flexDirection: 'column', gap: '8px' }}>
          <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)', fontWeight: 600, textTransform: 'uppercase', letterSpacing: '0.05em' }}>👥 Registered Users</span>
          <span style={{ fontSize: '2.2rem', fontWeight: 800, color: 'var(--success)' }}>{users.length}</span>
        </div>
        <div className="glass-panel" style={{ padding: '20px', display: 'flex', flexDirection: 'column', gap: '8px' }}>
          <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)', fontWeight: 600, textTransform: 'uppercase', letterSpacing: '0.05em' }}>🛡️ Total Admins</span>
          <span style={{ fontSize: '2.2rem', fontWeight: 800, color: 'var(--accent)' }}>{users.filter(u => u.roles && u.roles.some(r => r.name === 'ADMIN_USER')).length}</span>
        </div>
      </div>

      {/* Tabs Switcher */}
      <div style={{ display: 'flex', gap: '15px', marginBottom: '25px', borderBottom: '1px solid var(--border-glass)', paddingBottom: '12px' }}>
        <button
          className={`btn ${activeTab === 'categories' ? 'btn-primary' : 'btn-secondary'}`}
          onClick={() => {
            setActiveTab('categories');
            setError('');
            setSuccess('');
          }}
          style={{ borderRadius: '20px', padding: '8px 20px', fontSize: '0.9rem', fontWeight: 600 }}
        >
          📂 Category Manager ({categories.length})
        </button>
        <button
          className={`btn ${activeTab === 'posts' ? 'btn-primary' : 'btn-secondary'}`}
          onClick={() => {
            setActiveTab('posts');
            setError('');
            setSuccess('');
          }}
          style={{ borderRadius: '20px', padding: '8px 20px', fontSize: '0.9rem', fontWeight: 600 }}
        >
          📝 Article Moderator ({postPageInfo.totalElements})
        </button>
        <button
          className={`btn ${activeTab === 'users' ? 'btn-primary' : 'btn-secondary'}`}
          onClick={() => {
            setActiveTab('users');
            setError('');
            setSuccess('');
          }}
          style={{ borderRadius: '20px', padding: '8px 20px', fontSize: '0.9rem', fontWeight: 600 }}
        >
          👥 User Access Manager ({users.length})
        </button>
      </div>

      {/* Alert Notices */}
      {error && <div className="alert alert-danger" style={{ marginBottom: '20px' }}>{error}</div>}
      {success && <div className="alert alert-success" style={{ marginBottom: '20px' }}>{success}</div>}

      {/* TAB CONTENTS: CATEGORIES */}
      {activeTab === 'categories' && (
        <div className="admin-grid-layout" style={{ display: 'grid', gridTemplateColumns: '1.2fr 0.8fr', gap: '25px' }}>
          
          {/* Categories List */}
          <div className="glass-panel" style={{ padding: '25px' }}>
            <h3 style={{ marginBottom: '20px' }}>Current Categories</h3>
            {loadingCats ? (
              <p style={{ color: 'var(--text-muted)' }}>Loading categories...</p>
            ) : categories.length === 0 ? (
              <p style={{ color: 'var(--text-muted)', fontStyle: 'italic' }}>No categories registered in the database.</p>
            ) : (
              <div style={{ display: 'flex', flexDirection: 'column', gap: '15px' }}>
                {categories.map((cat) => (
                  <div key={cat.categoryId} className="glass-panel" style={{ padding: '15px', display: 'flex', justifyContent: 'space-between', alignItems: 'center', background: 'rgba(255, 255, 255, 0.02)', border: '1px solid rgba(255,255,255,0.05)' }}>
                    <div style={{ flex: 1, paddingRight: '15px' }}>
                      <h4 style={{ margin: '0 0 5px 0', color: 'var(--primary)' }}>{cat.categoryTitle}</h4>
                      <p style={{ margin: 0, fontSize: '0.88rem', color: 'var(--text-muted)' }}>{cat.categoryDecription}</p>
                    </div>
                    <div style={{ display: 'flex', gap: '10px' }}>
                      <button
                        onClick={() => handleStartEditCategory(cat)}
                        className="btn btn-secondary btn-sm"
                        style={{ padding: '5px 10px', fontSize: '0.8rem' }}
                      >
                        ✏️ Edit
                      </button>
                      <button
                        onClick={() => handleDeleteCategory(cat.categoryId)}
                        className="btn btn-danger btn-sm"
                        style={{ padding: '5px 10px', fontSize: '0.8rem' }}
                        disabled={actionLoading}
                      >
                        🗑️ Delete
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>

          {/* Create/Edit Category Form */}
          <div className="glass-panel" style={{ padding: '25px', height: 'fit-content' }}>
            <h3 style={{ marginBottom: '20px' }}>
              {editingCatId ? '📝 Edit Category' : '🆕 Create Category'}
            </h3>
            
            <form onSubmit={handleCatFormSubmit}>
              <div className="form-group" style={{ marginBottom: '15px' }}>
                <label className="form-label">Category Title</label>
                <input
                  type="text"
                  name="categoryTitle"
                  className="form-control"
                  placeholder="e.g. Technology"
                  value={catFormData.categoryTitle}
                  onChange={handleCatInputChange}
                  required
                />
              </div>

              <div className="form-group" style={{ marginBottom: '20px' }}>
                <label className="form-label">Category Description</label>
                <textarea
                  name="categoryDecription"
                  className="form-control"
                  placeholder="Describe what posts should go into this category..."
                  value={catFormData.categoryDecription}
                  onChange={handleCatInputChange}
                  required
                  style={{ minHeight: '120px' }}
                />
              </div>

              <div style={{ display: 'flex', gap: '10px' }}>
                <button
                  type="submit"
                  className="btn btn-primary"
                  style={{ flex: 1 }}
                  disabled={actionLoading}
                >
                  {editingCatId ? 'Update' : 'Create'}
                </button>
                {editingCatId && (
                  <button
                    type="button"
                    onClick={handleCancelEditCategory}
                    className="btn btn-secondary"
                    style={{ width: '90px' }}
                  >
                    Cancel
                  </button>
                )}
              </div>
            </form>
          </div>
        </div>
      )}

      {/* TAB CONTENTS: ARTICLES MODERATION */}
      {activeTab === 'posts' && (
        <div className="glass-panel" style={{ padding: '25px' }}>
          <h3 style={{ marginBottom: '20px' }}>All System Articles</h3>
          {loadingPosts ? (
            <p style={{ color: 'var(--text-muted)' }}>Loading system posts...</p>
          ) : posts.length === 0 ? (
            <p style={{ color: 'var(--text-muted)', fontStyle: 'italic' }}>No posts published in the system yet.</p>
          ) : (
            <div>
              {/* Responsive custom list or table */}
              <div className="admin-posts-table-wrapper" style={{ overflowX: 'auto' }}>
                <table className="admin-posts-table" style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', marginBottom: '20px' }}>
                  <thead>
                    <tr style={{ borderBottom: '2px solid var(--border-glass)' }}>
                      <th style={{ padding: '12px 8px', color: 'var(--text-muted)', fontWeight: 600 }}>Title</th>
                      <th style={{ padding: '12px 8px', color: 'var(--text-muted)', fontWeight: 600 }}>Author</th>
                      <th style={{ padding: '12px 8px', color: 'var(--text-muted)', fontWeight: 600 }}>Category</th>
                      <th style={{ padding: '12px 8px', color: 'var(--text-muted)', fontWeight: 600 }}>Date</th>
                      <th style={{ padding: '12px 8px', color: 'var(--text-muted)', fontWeight: 600, textAlign: 'center' }}>Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {posts.map((post) => (
                      <tr key={post.postId} style={{ borderBottom: '1px solid rgba(255, 255, 255, 0.05)' }}>
                        <td style={{ padding: '15px 8px', fontWeight: 500 }}>
                          <Link to={`/posts/${post.postId}`} className="nav-item-active" style={{ color: '#fff', textDecoration: 'none' }}>
                            {post.title}
                          </Link>
                        </td>
                        <td style={{ padding: '15px 8px', color: 'var(--text-muted)' }}>{post.user?.name || 'Anonymous'}</td>
                        <td style={{ padding: '15px 8px' }}>
                          <span className="category-tag" style={{ fontSize: '0.8rem', padding: '3px 8px' }}>
                            {post.category?.categoryTitle || 'General'}
                          </span>
                        </td>
                        <td style={{ padding: '15px 8px', color: 'var(--text-muted)', fontSize: '0.85rem' }}>
                          {post.addDate ? new Date(post.addDate).toLocaleDateString() : 'N/A'}
                        </td>
                        <td style={{ padding: '15px 8px', textAlign: 'center' }}>
                          <div style={{ display: 'flex', gap: '8px', justifyContent: 'center' }}>
                            <Link
                              to={`/posts/${post.postId}`}
                              className="btn btn-secondary btn-sm"
                              style={{ padding: '4px 8px', fontSize: '0.75rem' }}
                            >
                              👁️ View
                            </Link>
                            <Link
                              to={`/edit-post/${post.postId}`}
                              className="btn btn-secondary btn-sm"
                              style={{ padding: '4px 8px', fontSize: '0.75rem' }}
                            >
                              ✏️ Edit
                            </Link>
                            <button
                              onClick={() => handleDeletePost(post.postId)}
                              className="btn btn-danger btn-sm"
                              style={{ padding: '4px 8px', fontSize: '0.75rem' }}
                              disabled={actionLoading}
                            >
                              🗑️ Delete
                            </button>
                          </div>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>

              {/* Pagination Controls */}
              {postPageInfo.totalPages > 1 && (
                <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', gap: '15px', marginTop: '20px' }}>
                  <button
                    onClick={() => loadPosts(postPageInfo.pageNumber - 1)}
                    disabled={postPageInfo.pageNumber === 0}
                    className="btn btn-secondary btn-sm"
                    style={{ padding: '5px 12px' }}
                  >
                    ◀ Prev
                  </button>
                  <span style={{ color: 'var(--text-muted)' }}>
                    Page {postPageInfo.pageNumber + 1} of {postPageInfo.totalPages}
                  </span>
                  <button
                    onClick={() => loadPosts(postPageInfo.pageNumber + 1)}
                    disabled={postPageInfo.lastPage}
                    className="btn btn-secondary btn-sm"
                    style={{ padding: '5px 12px' }}
                  >
                    Next ▶
                  </button>
                </div>
              )}
            </div>
          )}
        </div>
      )}

      {/* TAB CONTENTS: USER ACCESS MANAGER */}
      {activeTab === 'users' && (
        <div className="glass-panel" style={{ padding: '25px' }}>
          <h3 style={{ marginBottom: '20px' }}>Registered User Directory</h3>
          {loadingUsers ? (
            <p style={{ color: 'var(--text-muted)' }}>Loading users list...</p>
          ) : users.length === 0 ? (
            <p style={{ color: 'var(--text-muted)', fontStyle: 'italic' }}>No registered users found.</p>
          ) : (
            <div className="admin-posts-table-wrapper" style={{ overflowX: 'auto' }}>
              <table className="admin-posts-table" style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', marginBottom: '20px' }}>
                <thead>
                  <tr style={{ borderBottom: '2px solid var(--border-glass)' }}>
                    <th style={{ padding: '12px 8px', color: 'var(--text-muted)', fontWeight: 600 }}>Name</th>
                    <th style={{ padding: '12px 8px', color: 'var(--text-muted)', fontWeight: 600 }}>Email</th>
                    <th style={{ padding: '12px 8px', color: 'var(--text-muted)', fontWeight: 600 }}>About</th>
                    <th style={{ padding: '12px 8px', color: 'var(--text-muted)', fontWeight: 600 }}>Roles</th>
                    <th style={{ padding: '12px 8px', color: 'var(--text-muted)', fontWeight: 600, textAlign: 'center' }}>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {users.map((u) => {
                    const isAdmin = u.roles && u.roles.some((r) => r.name === 'ADMIN_USER');
                    return (
                      <tr key={u.id} style={{ borderBottom: '1px solid rgba(255, 255, 255, 0.05)' }}>
                        <td style={{ padding: '15px 8px', fontWeight: 500, color: '#fff' }}>
                          {u.name}
                        </td>
                        <td style={{ padding: '15px 8px', color: 'var(--text-muted)' }}>{u.email}</td>
                        <td style={{ padding: '15px 8px', color: 'var(--text-muted)', fontSize: '0.9rem', maxWidth: '250px', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                          {u.about || 'N/A'}
                        </td>
                        <td style={{ padding: '15px 8px' }}>
                          <div style={{ display: 'flex', gap: '5px', flexWrap: 'wrap' }}>
                            {u.roles && u.roles.map((role) => (
                              <span
                                key={role.id}
                                className="category-tag"
                                style={{
                                  fontSize: '0.75rem',
                                  padding: '2px 8px',
                                  borderRadius: '4px',
                                  background: role.name === 'ADMIN_USER' 
                                    ? 'rgba(244, 63, 94, 0.15)' 
                                    : 'rgba(99, 102, 241, 0.15)',
                                  color: role.name === 'ADMIN_USER' 
                                    ? 'var(--accent)' 
                                    : 'var(--primary)',
                                  border: role.name === 'ADMIN_USER'
                                    ? '1px solid rgba(244, 63, 94, 0.3)'
                                    : '1px solid rgba(99, 102, 241, 0.3)'
                                }}
                              >
                                {role.name === 'ADMIN_USER' ? '🛡️ Admin' : '👤 User'}
                              </span>
                            ))}
                          </div>
                        </td>
                        <td style={{ padding: '15px 8px', textAlign: 'center' }}>
                          <div style={{ display: 'flex', gap: '8px', justifyContent: 'center', alignItems: 'center' }}>
                            {!isAdmin ? (
                              <button
                                onClick={() => handlePromoteUser(u.id, u.name)}
                                className="btn btn-primary btn-sm"
                                style={{ padding: '4px 10px', fontSize: '0.75rem', background: 'linear-gradient(135deg, var(--success) 0%, #059669 100%)' }}
                                disabled={actionLoading}
                              >
                                🛡️ Promote to Admin
                              </button>
                            ) : (
                              <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)', fontStyle: 'italic', padding: '4px 10px' }}>
                                Already Admin
                              </span>
                            )}
                            <button
                              onClick={() => handleDeleteUser(u.id, u.name)}
                              className="btn btn-danger btn-sm"
                              style={{ padding: '4px 10px', fontSize: '0.75rem' }}
                              disabled={actionLoading}
                            >
                              🗑️ Delete
                            </button>
                          </div>
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          )}
        </div>
      )}
    </div>
  );
};

export default AdminDashboard;
